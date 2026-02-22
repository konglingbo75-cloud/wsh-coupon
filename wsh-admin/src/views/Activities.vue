<template>
  <div class="activities-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索活动名称" clearable style="width: 220px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="fetchData">
              <el-option label="草稿" :value="0" />
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="2" />
              <el-option label="结束" :value="3" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="activityName" label="活动名称" min-width="160" />
        <el-table-column prop="merchantName" label="商户" width="130" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">{{ typeText(row.activityType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存/已售" width="100" align="center">
          <template #default="{ row }">
            {{ row.stock === -1 ? '不限' : row.stock }} / {{ row.soldCount }}
          </template>
        </el-table-column>
        <el-table-column label="公开" width="60" align="center">
          <template #default="{ row }">
            <el-icon :style="{ color: row.isPublic ? '#67c23a' : '#909399' }">
              <component :is="row.isPublic ? 'CircleCheck' : 'CircleClose'" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getActivities, type ActivityItem } from '@/api/admin'

const loading = ref(false)
const tableData = ref<ActivityItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: null as number | null })

function typeText(type: number) {
  const map: Record<number, string> = { 1: '代金券', 2: '储值', 3: '积分兑换', 4: '团购' }
  return map[type] || '未知'
}
function statusText(s: number) {
  const map: Record<number, string> = { 0: '草稿', 1: '上架', 2: '下架', 3: '结束' }
  return map[s] || '未知'
}
function statusType(s: number): 'info' | 'success' | 'warning' | 'danger' {
  const map: Record<number, 'info' | 'success' | 'warning' | 'danger'> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getActivities(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch { /* */ } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
