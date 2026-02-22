<template>
  <view class="record-page">
    <!-- 统计概览 -->
    <view class="stats-card">
      <view class="stats-header">
        <text class="title">今日核销</text>
        <picker mode="date" :value="selectedDate" @change="onDateChange">
          <view class="date-picker">
            <text>{{ selectedDate }}</text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>
      <view class="stats-body">
        <view class="stat-item">
          <text class="num">{{ stats.totalCount }}</text>
          <text class="label">核销笔数</text>
        </view>
        <view class="stat-item">
          <text class="num">¥{{ stats.totalAmount }}</text>
          <text class="label">核销金额</text>
        </view>
        <view class="stat-item">
          <text class="num">{{ stats.successRate }}%</text>
          <text class="label">成功率</text>
        </view>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: currentStatus === 'all' }" @click="currentStatus = 'all'">
        <text>全部</text>
      </view>
      <view class="filter-item" :class="{ active: currentStatus === 'success' }" @click="currentStatus = 'success'">
        <text>成功</text>
      </view>
      <view class="filter-item" :class="{ active: currentStatus === 'fail' }" @click="currentStatus = 'fail'">
        <text>失败</text>
      </view>
    </view>

    <!-- 记录列表 -->
    <view class="record-list">
      <view v-for="item in records" :key="item.id" class="record-card">
        <view class="card-header">
          <view class="voucher-info">
            <text class="name">{{ item.voucherName }}</text>
            <text class="type">{{ getTypeName(item.voucherType) }}</text>
          </view>
          <view class="status-badge" :class="item.status">
            {{ item.status === 'success' ? '成功' : '失败' }}
          </view>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="label">券码</text>
            <text class="value">{{ item.code }}</text>
          </view>
          <view class="info-row">
            <text class="label">用户手机</text>
            <text class="value">{{ item.userPhone }}</text>
          </view>
          <view class="info-row">
            <text class="label">核销金额</text>
            <text class="value amount">¥{{ item.amount }}</text>
          </view>
          <view class="info-row">
            <text class="label">核销时间</text>
            <text class="value">{{ item.time }}</text>
          </view>
          <view class="info-row">
            <text class="label">操作员</text>
            <text class="value">{{ item.operator }}</text>
          </view>
        </view>
        <view v-if="item.status === 'fail'" class="fail-reason">
          <text class="label">失败原因:</text>
          <text class="reason">{{ item.failReason }}</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && records.length === 0" class="empty-state">
        <image src="/static/empty-record.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无核销记录</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="records.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @click="loadMore">加载更多</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get } from '@/api/request'

interface RecordItem {
  id: string
  voucherName: string
  voucherType: 'voucher' | 'deposit' | 'points'
  code: string
  userPhone: string
  amount: number
  time: string
  operator: string
  status: 'success' | 'fail'
  failReason?: string
}

interface Stats {
  totalCount: number
  totalAmount: number
  successRate: number
}

const selectedDate = ref(new Date().toISOString().split('T')[0])
const currentStatus = ref('all')
const stats = ref<Stats>({ totalCount: 0, totalAmount: 0, successRate: 100 })
const records = ref<RecordItem[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    voucher: '优惠券',
    deposit: '储值',
    points: '积分'
  }
  return map[type] || type
}

const onDateChange = (e: any) => {
  selectedDate.value = e.detail.value
}

const loadStats = async () => {
  try {
    const res = await get<Stats>('/merchant/verification/stats', {
      date: selectedDate.value
    })
    if (res.code === 0) {
      stats.value = res.data
    }
  } catch (e) {
    // 忽略
  }
}

const loadRecords = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    noMore.value = false
    records.value = []
  }
  
  loading.value = true
  try {
    const res = await get<{ list: RecordItem[] }>('/merchant/verification/records', {
      date: selectedDate.value,
      status: currentStatus.value === 'all' ? undefined : currentStatus.value,
      page: page.value,
      pageSize
    })
    
    if (res.code === 0) {
      const list = res.data.list || []
      if (reset) {
        records.value = list
      } else {
        records.value = [...records.value, ...list]
      }
      
      if (list.length < pageSize) {
        noMore.value = true
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (noMore.value || loading.value) return
  page.value++
  loadRecords()
}

watch([selectedDate, currentStatus], () => {
  loadStats()
  loadRecords(true)
})

onMounted(() => {
  loadStats()
  loadRecords(true)
})
</script>

<style lang="scss">
.record-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-card {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  color: #fff;
  
  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title {
      font-size: 30rpx;
      font-weight: bold;
    }
    
    .date-picker {
      display: flex;
      align-items: center;
      padding: 8rpx 16rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      font-size: 26rpx;
      
      .arrow {
        margin-left: 8rpx;
        font-size: 20rpx;
      }
    }
  }
  
  .stats-body {
    display: flex;
    justify-content: space-around;
    
    .stat-item {
      text-align: center;
      
      .num {
        display: block;
        font-size: 40rpx;
        font-weight: bold;
      }
      
      .label {
        display: block;
        font-size: 24rpx;
        opacity: 0.9;
        margin-top: 8rpx;
      }
    }
  }
}

.filter-bar {
  display: flex;
  background: #fff;
  margin: 0 24rpx;
  border-radius: 12rpx;
  padding: 8rpx;
  
  .filter-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    font-size: 28rpx;
    color: #666;
    border-radius: 8rpx;
    
    &.active {
      background: #ff6b35;
      color: #fff;
    }
  }
}

.record-list {
  padding: 24rpx;
}

.record-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .voucher-info {
      display: flex;
      align-items: center;
      
      .name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .type {
        margin-left: 12rpx;
        padding: 4rpx 12rpx;
        background: #fff3e0;
        color: #f7931e;
        font-size: 22rpx;
        border-radius: 8rpx;
      }
    }
    
    .status-badge {
      padding: 6rpx 16rpx;
      border-radius: 16rpx;
      font-size: 24rpx;
      
      &.success {
        background: #e8f5e9;
        color: #4caf50;
      }
      
      &.fail {
        background: #ffebee;
        color: #f44336;
      }
    }
  }
  
  .card-body {
    padding: 16rpx 20rpx;
    
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 8rpx 0;
      
      .label {
        font-size: 26rpx;
        color: #999;
      }
      
      .value {
        font-size: 26rpx;
        color: #333;
        
        &.amount {
          font-weight: bold;
          color: #ff6b35;
        }
      }
    }
  }
  
  .fail-reason {
    padding: 16rpx 20rpx;
    background: #fff5f5;
    display: flex;
    
    .label {
      font-size: 24rpx;
      color: #f44336;
      flex-shrink: 0;
    }
    
    .reason {
      font-size: 24rpx;
      color: #f44336;
      margin-left: 8rpx;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  
  .empty-icon {
    width: 200rpx;
    height: 200rpx;
    opacity: 0.6;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 24rpx;
  }
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
