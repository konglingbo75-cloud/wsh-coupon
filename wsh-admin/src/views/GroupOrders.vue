<template>
  <div class="grouporders-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>拼团管理</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索拼团编号/活动名称" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
              <el-option label="拼团中" :value="0" />
              <el-option label="已成团" :value="1" />
              <el-option label="已失败" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="groupNo" label="拼团编号" width="180" />
        <el-table-column prop="activityName" label="活动名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商户" width="120" show-overflow-tooltip />
        <el-table-column prop="initiatorNickname" label="发起人" width="100" />
        <el-table-column label="进度" width="100" align="center">
          <template #default="{ row }">
            <span :class="progressClass(row)">{{ row.currentMembers }}/{{ row.requiredMembers }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="截止时间" width="160" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="拼团详情" width="600px">
      <div class="detail-content" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="拼团编号">{{ detailData.groupNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detailData.status).type">{{ detailData.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动名称" :span="2">{{ detailData.activityName }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ detailData.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ detailData.initiatorNickname }}</el-descriptions-item>
          <el-descriptions-item label="拼团进度">{{ detailData.currentMembers }}/{{ detailData.requiredMembers }} 人</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ detailData.expireTime }}</el-descriptions-item>
          <el-descriptions-item label="成团时间">{{ detailData.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <div class="participants-section">
          <h4>参与者列表</h4>
          <el-table :data="detailData.participants" size="small" stripe>
            <el-table-column label="头像" width="60" align="center">
              <template #default="{ row }">
                <el-avatar :size="32" :src="row.avatarUrl || undefined">{{ row.nickname?.charAt(0) }}</el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="昵称" width="100" />
            <el-table-column label="角色" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isInitiator" type="danger" size="small">团长</el-tag>
                <el-tag v-else type="info" size="small">成员</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.orderId && row.orderStatus === 1" type="success" size="small">已支付</el-tag>
                <el-tag v-else-if="row.orderId" type="warning" size="small">待支付</el-tag>
                <el-tag v-else type="info" size="small">未下单</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" width="160" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getGroupOrders, getGroupOrderDetail, type GroupOrderItem, type GroupOrderDetail } from '@/api/admin'

const loading = ref(false)
const tableData = ref<GroupOrderItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: null as number | null })

const detailVisible = ref(false)
const detailData = ref<GroupOrderDetail | null>(null)

function statusTag(status: number) {
  const map: Record<number, { type: 'warning' | 'success' | 'danger' | 'info'; text: string }> = {
    0: { type: 'warning', text: '拼团中' },
    1: { type: 'success', text: '已成团' },
    2: { type: 'danger', text: '已失败' },
    3: { type: 'info', text: '已取消' }
  }
  return map[status] || { type: 'info', text: '未知' }
}

function progressClass(row: GroupOrderItem) {
  if (row.currentMembers >= row.requiredMembers) return 'progress-full'
  if (row.currentMembers > row.requiredMembers / 2) return 'progress-half'
  return 'progress-low'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getGroupOrders(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch { /* */ } finally {
    loading.value = false
  }
}

async function showDetail(row: GroupOrderItem) {
  try {
    const res = await getGroupOrderDetail(row.groupOrderId)
    detailData.value = res.data
    detailVisible.value = true
  } catch { /* */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.progress-full { color: #67c23a; font-weight: 600; }
.progress-half { color: #e6a23c; font-weight: 500; }
.progress-low { color: #909399; }
.participants-section {
  margin-top: 20px;
}
.participants-section h4 {
  margin-bottom: 12px;
  color: #303133;
}
</style>
