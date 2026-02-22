<template>
  <div class="billing-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>费用管理</span>
          <div class="header-actions">
            <el-select v-model="query.status" placeholder="支付状态" clearable style="width: 140px" @change="fetchData">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已关闭" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column prop="planName" label="套餐" width="130" />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">&yen;{{ row.feeAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="payStatusType(row.payStatus)">{{ payStatusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.validStartDate">{{ row.validStartDate }} ~ {{ row.validEndDate }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
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
import { getBillings, type BillingItem } from '@/api/admin'

const loading = ref(false)
const tableData = ref<BillingItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, status: null as number | null })

function payStatusText(s: number) {
  const map: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已关闭' }
  return map[s] || '未知'
}
function payStatusType(s: number): 'warning' | 'success' | 'info' {
  const map: Record<number, 'warning' | 'success' | 'info'> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getBillings(query)
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
