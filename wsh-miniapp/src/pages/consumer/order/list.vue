<template>
  <view class="order-list-page">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view 
        v-for="tab in statusTabs" 
        :key="tab.key" 
        class="filter-item" 
        :class="{ active: currentStatus === tab.key }"
        @click="currentStatus = tab.key"
      >
        <text>{{ tab.label }}</text>
        <view v-if="tab.count > 0" class="badge">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view 
      class="order-list" 
      scroll-y 
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-for="order in orders" :key="order.id" class="order-card" @click="goDetail(order.id)">
        <view class="card-header">
          <view class="merchant-info">
            <image class="logo" :src="order.merchantLogo" mode="aspectFill" />
            <text class="name">{{ order.merchantName }}</text>
          </view>
          <text class="status" :class="order.status">{{ getStatusText(order.status) }}</text>
        </view>
        
        <view class="card-body">
          <image class="cover" :src="order.coverUrl" mode="aspectFill" />
          <view class="info">
            <text class="title">{{ order.title }}</text>
            <text class="spec">{{ order.spec }}</text>
            <view class="price-row">
              <text class="price">¥{{ order.price }}</text>
              <text class="quantity">x{{ order.quantity }}</text>
            </view>
          </view>
        </view>
        
        <view class="card-footer">
          <text class="total">共{{ order.quantity }}件 实付: <text class="amount">¥{{ order.totalAmount }}</text></text>
          <view class="actions">
            <button v-if="order.status === 'pending'" class="action-btn primary" @click.stop="handlePay(order)">去支付</button>
            <button v-if="order.status === 'pending'" class="action-btn" @click.stop="handleCancel(order)">取消</button>
            <button v-if="order.status === 'paid'" class="action-btn primary" @click.stop="handleUse(order)">去使用</button>
            <button v-if="order.status === 'completed'" class="action-btn" @click.stop="handleReview(order)">去评价</button>
            <button v-if="order.status === 'cancelled'" class="action-btn" @click.stop="handleRebuy(order)">再次购买</button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && orders.length === 0" class="empty-state">
        <image src="/static/empty-order.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无订单</text>
        <button class="go-btn" @click="goActivity">去逛逛</button>
      </view>

      <!-- 加载更多 -->
      <view v-if="orders.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { getOrders, cancelOrder, requestPayment } from '@/api/order'

interface OrderItem {
  id: string
  merchantId: string
  merchantName: string
  merchantLogo: string
  title: string
  coverUrl: string
  spec: string
  price: number
  quantity: number
  totalAmount: number
  status: 'pending' | 'paid' | 'used' | 'completed' | 'cancelled' | 'refunded'
  createTime: string
}

const statusTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'pending', label: '待支付', count: 0 },
  { key: 'paid', label: '待使用', count: 0 },
  { key: 'completed', label: '已完成', count: 0 },
  { key: 'cancelled', label: '已取消', count: 0 }
])

const currentStatus = ref('all')
const orders = ref<OrderItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待支付',
    paid: '待使用',
    used: '已使用',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[status] || status
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
    const res = await getOrders({
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
      
      // 更新各状态数量
      if (res.data.statusCounts) {
        statusTabs.value = statusTabs.value.map(tab => ({
          ...tab,
          count: res.data.statusCounts[tab.key] || 0
        }))
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = async () => {
  refreshing.value = true
  await loadOrders(true)
}

const loadMore = () => {
  if (noMore.value || loading.value) return
  page.value++
  loadOrders()
}

const goDetail = (orderId: string) => {
  uni.navigateTo({ url: `/pages/consumer/order/detail?id=${orderId}` })
}

const handlePay = async (order: OrderItem) => {
  try {
    const res = await requestPayment(order.id)
    if (res.code === 0) {
      await uni.requestPayment({
        provider: 'wxpay',
        ...res.data.payParams
      })
      uni.showToast({ title: '支付成功', icon: 'success' })
      loadOrders(true)
    }
  } catch (e: any) {
    if (!e.errMsg?.includes('cancel')) {
      uni.showToast({ title: '支付失败', icon: 'none' })
    }
  }
}

const handleCancel = async (order: OrderItem) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await cancelOrder(order.id)
          if (result.code === 0) {
            uni.showToast({ title: '已取消', icon: 'success' })
            loadOrders(true)
          }
        } catch (e) {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

const handleUse = (order: OrderItem) => {
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?orderId=${order.id}` })
}

const handleReview = (order: OrderItem) => {
  uni.navigateTo({ url: `/pages/consumer/order/review?orderId=${order.id}` })
}

const handleRebuy = (order: OrderItem) => {
  uni.navigateTo({ url: `/pages/consumer/activity/detail?id=${order.id}` })
}

const goActivity = () => {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

watch(currentStatus, () => {
  loadOrders(true)
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const status = currentPage.options?.status
  if (status && statusTabs.value.some(t => t.key === status)) {
    currentStatus.value = status
  }
  loadOrders(true)
})
</script>

<style lang="scss">
.order-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 0 12rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .filter-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24rpx 0;
    position: relative;
    font-size: 28rpx;
    color: #666;
    
    &.active {
      color: #ff6b35;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 48rpx;
        height: 4rpx;
        background: #ff6b35;
        border-radius: 2rpx;
      }
    }
    
    .badge {
      position: absolute;
      top: 16rpx;
      right: 8rpx;
      min-width: 32rpx;
      height: 32rpx;
      background: #ff4757;
      color: #fff;
      font-size: 20rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 8rpx;
    }
  }
}

.order-list {
  flex: 1;
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
    padding: 20rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .merchant-info {
      display: flex;
      align-items: center;
      
      .logo {
        width: 40rpx;
        height: 40rpx;
        border-radius: 6rpx;
      }
      
      .name {
        margin-left: 12rpx;
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
      }
    }
    
    .status {
      font-size: 26rpx;
      
      &.pending { color: #ff9800; }
      &.paid { color: #4caf50; }
      &.used, &.completed { color: #999; }
      &.cancelled, &.refunded { color: #999; }
    }
  }
  
  .card-body {
    display: flex;
    padding: 20rpx;
    
    .cover {
      width: 160rpx;
      height: 160rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      margin-left: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .title {
        font-size: 28rpx;
        color: #333;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .spec {
        font-size: 24rpx;
        color: #999;
      }
      
      .price-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .price {
          font-size: 30rpx;
          font-weight: bold;
          color: #333;
        }
        
        .quantity {
          font-size: 26rpx;
          color: #999;
        }
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx;
    border-top: 1rpx solid #f5f5f5;
    
    .total {
      font-size: 26rpx;
      color: #666;
      
      .amount {
        font-size: 30rpx;
        font-weight: bold;
        color: #ff6b35;
      }
    }
    
    .actions {
      display: flex;
      
      .action-btn {
        margin-left: 16rpx;
        padding: 12rpx 24rpx;
        font-size: 24rpx;
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
  padding: 100rpx 40rpx;
  
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
  
  .go-btn {
    margin-top: 32rpx;
    padding: 16rpx 48rpx;
    background: #ff6b35;
    color: #fff;
    font-size: 26rpx;
    border-radius: 32rpx;
    border: none;
  }
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
