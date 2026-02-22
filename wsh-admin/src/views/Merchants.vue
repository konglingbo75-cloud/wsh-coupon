<template>
  <div class="merchants-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>商户管理</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索商户名/编码/联系人" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px" @change="fetchData">
              <el-option label="待审核" :value="0" />
              <el-option label="正常" :value="1" />
              <el-option label="冻结" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="merchantCode" label="商户编码" width="160" />
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="city" label="城市" width="80" />
        <el-table-column prop="businessCategory" label="类目" width="80" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type">{{ statusTag(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="branchCount" label="门店" width="60" align="center" />
        <el-table-column prop="activityCount" label="活动" width="60" align="center" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/merchants/${row.merchantId}`)">详情</el-button>
            <el-button link type="success" v-if="row.status === 0" @click="handleAudit(row, 'APPROVE')">通过</el-button>
            <el-button link type="danger" v-if="row.status === 0" @click="handleAudit(row, 'REJECT')">拒绝</el-button>
            <el-button link type="warning" v-if="row.status === 1" @click="handleStatus(row, 'FREEZE')">冻结</el-button>
            <el-button link type="success" v-if="row.status === 2" @click="handleStatus(row, 'UNFREEZE')">解冻</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchants, auditMerchant, updateMerchantStatus, type MerchantItem } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const tableData = ref<MerchantItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: null as number | null })

function statusTag(status: number) {
  const map: Record<number, { type: 'warning' | 'success' | 'danger'; text: string }> = {
    0: { type: 'warning', text: '待审核' },
    1: { type: 'success', text: '正常' },
    2: { type: 'danger', text: '冻结' }
  }
  return map[status] || { type: 'warning', text: '未知' }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMerchants(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch { /* */ } finally {
    loading.value = false
  }
}

async function handleAudit(row: MerchantItem, action: string) {
  const actionText = action === 'APPROVE' ? '通过' : '拒绝'
  try {
    const result = await ElMessageBox.prompt(`请输入${actionText}原因`, `审核${actionText}`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因（可选）'
    })
    const reason = (result as any).value as string
    await auditMerchant({ merchantId: row.merchantId, action, reason })
    ElMessage.success(`已${actionText}商户：${row.merchantName}`)
    fetchData()
  } catch { /* cancelled */ }
}

async function handleStatus(row: MerchantItem, action: string) {
  const actionText = action === 'FREEZE' ? '冻结' : '解冻'
  try {
    const result = await ElMessageBox.prompt(`请输入${actionText}原因`, `${actionText}商户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因'
    })
    const reason = (result as any).value as string
    await updateMerchantStatus({ merchantId: row.merchantId, action, reason })
    ElMessage.success(`已${actionText}商户：${row.merchantName}`)
    fetchData()
  } catch { /* cancelled */ }
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
</style>
