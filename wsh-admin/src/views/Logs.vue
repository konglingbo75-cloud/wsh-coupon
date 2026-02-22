<template>
  <div class="logs-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索模块/操作/详情" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="adminName" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作" width="140" />
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="时间" width="170" />
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
import { getOperationLogs, type OperationLogItem } from '@/api/admin'

const loading = ref(false)
const tableData = ref<OperationLogItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getOperationLogs(query)
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
