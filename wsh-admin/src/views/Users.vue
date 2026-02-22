<template>
  <div class="users-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索昵称/手机号" clearable style="width: 220px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-button type="primary" @click="fetchData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="memberLevel" label="会员等级" width="90" align="center" />
        <el-table-column prop="merchantCount" label="关联商户" width="90" align="center" />
        <el-table-column prop="totalConsumeCount" label="消费次数" width="90" align="center" />
        <el-table-column label="消费总额" width="120" align="right">
          <template #default="{ row }">
            {{ row.totalConsumeAmount != null ? `¥${row.totalConsumeAmount.toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
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
import { getUsers, type UserItem } from '@/api/admin'

const loading = ref(false)
const tableData = ref<UserItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getUsers(query)
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
