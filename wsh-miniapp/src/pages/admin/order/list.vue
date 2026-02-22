<template>
  <view class="order-manage-page">
    <!-- 统计概览 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="num">{{ stats.todayCount }}</text>
        <text class="label">今日订单</text>
      </view>
      <view class="stat-item">
        <text class="num">¥{{ stats.todayRevenue }}</text>
        <text class="label">今日收入</text>
      </view>
      <view class="stat-item">
        <text class="num">{{ stats.pendingVerify }}</text>
        <text class="label">待核销</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view 
        v-for="tab in statusTabs" 
        :key="tab.key" 
        class="filter-item" 
        :class="{ active: currentStatus === tab.key }"
        @click="currentStatus = tab.key"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view v-for="item in orders" :key="item.id" class="order-card">
        <view class="card-header">
          <text class="order-no">订单号: {{ item.orderNo }}</text>
          <text class="status" :class="item.status">{{ getStatusText(item.status) }}</text>
        </view>
        
        <view class="card-body">
          <view class="product-info">
            <image class="cover" :src="item.coverUrl" mode="aspectFill" />
            <view class="info">
              <text class="title">{{ item.title }}</text>
              <text class="spec">{{ item.spec }} x{{ item.quantity }}</text>
            </view>
            <text class="amount">¥{{ item.totalAmount }}</text>
          </view>
          
          <view class="user-info">
            <text class="label">买家:</text>
            <text class="value">{{ item.userPhone }}</text>
            <text class="time">{{ item.createTime }}</text>
          </view>
        </view>
        
        <view class="card-footer">
          <view class="voucher-codes" v-if="item.voucherCodes && item.voucherCodes.length > 0">
            <text class="label">券码:</text>
            <text class="codes">{{ item.voucherCodes.join(', ') }}</text>
          </view>
          <view class="actions">
            <button v-if="item.status === 'paid'" class="action-btn primary" @click="verifyOrder(item)">核销</button>
            <button class="action-btn" @click="viewDetail(item)">详情</button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && orders.length === 0" class="empty-state">
        <image src="/static/empty-order.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无订单</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="orders.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface Stats {
  todayCount: number
  todayRevenue: number
  pendingVerify: number
}

interface OrderItem {
  id: string
  orderNo: string
  title: string
  coverUrl: string
  spec: string
  quantity: number
  totalAmount: number
  userPhone: string
  createTime: string
  status: 'pending' | 'paid' | 'verified' | 'completed' | 'cancelled' | 'refunded'
  voucherCodes?: string[]
}

const statusTabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待支付' },
  { key: 'paid', label: '待核销' },
  { key: 'verified', label: '已核销' },
  { key: 'completed', label: '已完成' }
]

const stats = ref<Stats>({ todayCount: 0, todayRevenue: 0, pendingVerify: 0 })
const currentStatus = ref('all')
const orders = ref<OrderItem[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待支付',
    paid: '待核销',
    verified: '已核销',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[status] || status
}

const loadStats = async () => {
  try {
    const res = await get<Stats>('/admin/orders/stats')
    if (res.code === 0) {
      stats.value = res.data
    }
  } catch (e) {
    // 忽略
  }
}

const loadOrders = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    noMore.value = false
    orders.value = []
  }
  
  loading.value = true
  try {
    const res = await get<{ list: OrderItem[] }>('/admin/orders', {
      status: currentStatus.value === 'all' ? undefined : currentStatus.value,
      page: page.value,
      pageSize
    })
    
    if (res.code === 0) {
      const list = res.data.list || []
      if (reset) {
        orders.value = list
      } else {
        orders.value = [...orders.value, ...list]
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

const verifyOrder = async (item: OrderItem) => {
  uni.showModal({
    title: '确认核销',
    content: `确定核销订单 ${item.orderNo} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await post(`/admin/orders/${item.id}/verify`)
          if (result.code === 0) {
            item.status = 'verified'
            uni.showToast({ title: '核销成功', icon: 'success' })
            loadStats()
          }
        } catch (e) {
          uni.showToast({ title: '核销失败', icon: 'none' })
        }
      }
    }
  })
}

const viewDetail = (item: OrderItem) => {
  uni.navigateTo({ url: `/pages/admin/order/detail?id=${item.id}` })
}

watch(currentStatus, () => {
  loadOrders(true)
})

onMounted(() => {
  loadStats()
  loadOrders(true)
})
</script>

<style lang="scss">
.order-manage-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-bar {
  display: flex;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  padding: 32rpx 24rpx;
  
  .stat-item {
    flex: 1;
    text-align: center;
    color: #fff;
    
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

.filter-bar {
  display: flex;
  background: #fff;
  padding: 0 12rpx;
  overflow-x: auto;
  white-space: nowrap;
  
  .filter-item {
    padding: 24rpx 20rpx;
    font-size: 28rpx;
    color: #666;
    position: relative;
    flex-shrink: 0;
    
    &.active {
      color: #ff6b35;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: #ff6b35;
        border-radius: 2rpx;
      }
    }
  }
}

.order-list {
  padding: 24rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 20rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .order-no {
      font-size: 24rpx;
      color: #999;
    }
    
    .status {
      font-size: 26rpx;
      
      &.pending { color: #ff9800; }
      &.paid { color: #4caf50; }
      &.verified { color: #2196f3; }
      &.completed { color: #999; }
      &.cancelled, &.refunded { color: #999; }
    }
  }
  
  .card-body {
    padding: 20rpx;
    
    .product-info {
      display: flex;
      align-items: center;
      
      .cover {
        width: 120rpx;
        height: 120rpx;
        border-radius: 12rpx;
        flex-shrink: 0;
      }
      
      .info {
        flex: 1;
        margin-left: 16rpx;
        
        .title {
          display: block;
          font-size: 28rpx;
          color: #333;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .spec {
          display: block;
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
        }
      }
      
      .amount {
        font-size: 32rpx;
        font-weight: bold;
        color: #ff6b35;
        flex-shrink: 0;
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      margin-top: 16rpx;
      padding-top: 16rpx;
      border-top: 1rpx solid #f5f5f5;
      font-size: 24rpx;
      
      .label {
        color: #999;
      }
      
      .value {
        color: #333;
        margin-left: 8rpx;
      }
      
      .time {
        margin-left: auto;
        color: #999;
      }
    }
  }
  
  .card-footer {
    padding: 16rpx 20rpx;
    border-top: 1rpx solid #f5f5f5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .voucher-codes {
      flex: 1;
      font-size: 24rpx;
      
      .label {
        color: #999;
      }
      
      .codes {
        color: #333;
        margin-left: 8rpx;
      }
    }
    
    .actions {
      display: flex;
      
      .action-btn {
        margin-left: 16rpx;
        padding: 12rpx 24rpx;
        font-size: 26rpx;
        border-radius: 24rpx;
        background: #f5f5f5;
        color: #666;
        border: none;
        
        &.primary {
          background: linear-gradient(135deg, #ff6b35, #f7931e);
          color: #fff;
        }
      }
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
