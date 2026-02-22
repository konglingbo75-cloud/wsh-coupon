<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in topCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <el-icon class="stat-icon" :style="{ color: card.color }"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in bottomCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <el-icon class="stat-icon" :style="{ color: card.color }"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>商户状态分布</template>
          <div class="merchant-status">
            <div class="status-item" v-for="item in merchantStatusList" :key="item.label">
              <el-tag :type="item.tagType" size="large">{{ item.label }}</el-tag>
              <span class="status-count">{{ item.count }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>收入概览</template>
          <div class="income-overview">
            <div class="income-item">
              <div class="income-label">入驻费总收入</div>
              <div class="income-value">&yen;{{ stats?.totalOnboardingFee?.toFixed(2) || '0.00' }}</div>
            </div>
            <el-divider />
            <div class="income-item">
              <div class="income-label">服务费总收入</div>
              <div class="income-value">&yen;{{ stats?.totalServiceFee?.toFixed(2) || '0.00' }}</div>
            </div>
            <el-divider />
            <div class="income-item">
              <div class="income-label">订单总金额</div>
              <div class="income-value">&yen;{{ stats?.totalOrderAmount?.toFixed(2) || '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats, type DashboardStats } from '@/api/admin'

const stats = ref<DashboardStats | null>(null)

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch { /* handled by interceptor */ }
})

const topCards = computed(() => [
  { label: '商户总数', value: stats.value?.totalMerchants ?? '-', icon: 'Shop', color: '#409eff' },
  { label: '待审核商户', value: stats.value?.pendingMerchants ?? '-', icon: 'Bell', color: '#e6a23c' },
  { label: '用户总数', value: stats.value?.totalUsers ?? '-', icon: 'User', color: '#67c23a' },
  { label: '活动总数', value: stats.value?.totalActivities ?? '-', icon: 'Present', color: '#909399' }
])

const bottomCards = computed(() => [
  { label: '进行中活动', value: stats.value?.activeActivities ?? '-', icon: 'VideoPlay', color: '#67c23a' },
  { label: '订单总数', value: stats.value?.totalOrders ?? '-', icon: 'Tickets', color: '#409eff' },
  { label: '已核销券数', value: stats.value?.verifiedVouchers ?? '-', icon: 'CircleCheck', color: '#67c23a' },
  { label: '冻结商户', value: stats.value?.frozenMerchants ?? '-', icon: 'Lock', color: '#f56c6c' }
])

const merchantStatusList = computed(() => [
  { label: '待审核', count: stats.value?.pendingMerchants ?? 0, tagType: 'warning' as const },
  { label: '正常', count: stats.value?.activeMerchants ?? 0, tagType: 'success' as const },
  { label: '冻结', count: stats.value?.frozenMerchants ?? 0, tagType: 'danger' as const }
])
</script>

<style scoped>
.dashboard { }
.stat-row { margin-bottom: 20px; }
.stat-card { border-radius: 8px; }
.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.stat-icon { font-size: 40px; opacity: 0.8; }
.merchant-status {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}
.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-count { font-size: 20px; font-weight: bold; color: #303133; }
.income-overview { padding: 8px 0; }
.income-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.income-label { font-size: 14px; color: #606266; }
.income-value { font-size: 20px; font-weight: bold; color: #303133; }
</style>
