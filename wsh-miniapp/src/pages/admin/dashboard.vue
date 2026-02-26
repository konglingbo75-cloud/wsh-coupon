<template>
  <view class="dashboard-page">
    <!-- 商户信息头部 -->
    <view class="merchant-header">
      <image class="logo" :src="merchantInfo?.logoUrl || '/static/default-merchant.png'" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ merchantInfo?.name }}</text>
        <view class="status-badge" :class="merchantInfo?.status">
          {{ getStatusText(merchantInfo?.status) }}
        </view>
      </view>
      <view class="settings-btn" @click="goSettings">
        <text>⚙️</text>
      </view>
    </view>

    <!-- 数据概览 -->
    <view class="stats-section">
      <view class="stats-title">数据概览</view>
      <view class="date-tabs">
        <text :class="{ active: dateRange === 'today' }" @click="dateRange = 'today'">今日</text>
        <text :class="{ active: dateRange === 'week' }" @click="dateRange = 'week'">本周</text>
        <text :class="{ active: dateRange === 'month' }" @click="dateRange = 'month'">本月</text>
      </view>
      <view class="stats-grid">
        <view class="stat-card" @click="goOrders">
          <text class="num">{{ stats.orderCount }}</text>
          <text class="label">订单数</text>
          <text class="trend" :class="{ up: stats.orderTrend > 0 }">
            {{ stats.orderTrend > 0 ? '↑' : '↓' }}{{ Math.abs(stats.orderTrend) }}%
          </text>
        </view>
        <view class="stat-card" @click="goOrders">
          <text class="num">¥{{ stats.revenue }}</text>
          <text class="label">营收</text>
          <text class="trend" :class="{ up: stats.revenueTrend > 0 }">
            {{ stats.revenueTrend > 0 ? '↑' : '↓' }}{{ Math.abs(stats.revenueTrend) }}%
          </text>
        </view>
        <view class="stat-card" @click="goMembers">
          <text class="num">{{ stats.newMembers }}</text>
          <text class="label">新会员</text>
          <text class="trend" :class="{ up: stats.memberTrend > 0 }">
            {{ stats.memberTrend > 0 ? '↑' : '↓' }}{{ Math.abs(stats.memberTrend) }}%
          </text>
        </view>
        <view class="stat-card" @click="goVerification">
          <text class="num">{{ stats.verifyCount }}</text>
          <text class="label">核销数</text>
          <text class="trend" :class="{ up: stats.verifyTrend > 0 }">
            {{ stats.verifyTrend > 0 ? '↑' : '↓' }}{{ Math.abs(stats.verifyTrend) }}%
          </text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="entry-title">功能入口</view>
      <view class="entry-grid">
        <view class="entry-item" @click="goActivityList">
          <view class="icon-wrap activity">🎉</view>
          <text class="label">活动管理</text>
        </view>
        <view class="entry-item" @click="goVerification">
          <view class="icon-wrap scan">📷</view>
          <text class="label">扫码核销</text>
        </view>
        <view class="entry-item" @click="goMembers">
          <view class="icon-wrap member">👥</view>
          <text class="label">会员统计</text>
        </view>
        <view class="entry-item" @click="goOrders">
          <view class="icon-wrap order">📋</view>
          <text class="label">订单管理</text>
        </view>
        <view class="entry-item" @click="goBilling">
          <view class="icon-wrap billing">💰</view>
          <text class="label">付费管理</text>
        </view>
        <view class="entry-item" @click="goEmployeeBind">
          <view class="icon-wrap employee">👤</view>
          <text class="label">员工管理</text>
        </view>
        <view class="entry-item" @click="goSettings">
          <view class="icon-wrap settings">⚙️</view>
          <text class="label">设置</text>
        </view>
      </view>
    </view>

    <!-- 待办事项 -->
    <view v-if="todoList.length > 0" class="todo-section">
      <view class="section-header">
        <text class="title">待办事项</text>
        <text class="count">{{ todoList.length }}</text>
      </view>
      <view class="todo-list">
        <view v-for="item in todoList" :key="item.id" class="todo-item" @click="handleTodo(item)">
          <view class="todo-icon" :class="item.type">
            {{ getTodoIcon(item.type) }}
          </view>
          <view class="todo-content">
            <text class="title">{{ item.title }}</text>
            <text class="desc">{{ item.desc }}</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 最近订单 -->
    <view class="recent-orders">
      <view class="section-header">
        <text class="title">最近订单</text>
        <view class="more" @click="goOrders">
          <text>查看全部</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="order-list">
        <view v-for="item in recentOrders" :key="item.id" class="order-item">
          <view class="left">
            <text class="title">{{ item.title }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
          <view class="right">
            <text class="amount">+¥{{ item.amount }}</text>
            <text class="status">{{ item.status }}</text>
          </view>
        </view>
        <view v-if="recentOrders.length === 0" class="empty-order">
          <text>暂无订单</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get } from '@/api/request'

interface MerchantInfo {
  id: string
  name: string
  logoUrl: string
  status: 'active' | 'pending' | 'suspended'
}

interface Stats {
  orderCount: number
  orderTrend: number
  revenue: number
  revenueTrend: number
  newMembers: number
  memberTrend: number
  verifyCount: number
  verifyTrend: number
}

interface TodoItem {
  id: string
  type: 'order' | 'activity' | 'billing' | 'review'
  title: string
  desc: string
  targetUrl: string
}

interface OrderItem {
  id: string
  title: string
  amount: number
  time: string
  status: string
}

const merchantInfo = ref<MerchantInfo | null>(null)
const dateRange = ref('today')
const stats = ref<Stats>({
  orderCount: 0, orderTrend: 0,
  revenue: 0, revenueTrend: 0,
  newMembers: 0, memberTrend: 0,
  verifyCount: 0, verifyTrend: 0
})
const todoList = ref<TodoItem[]>([])
const recentOrders = ref<OrderItem[]>([])

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    active: '正常营业',
    pending: '审核中',
    suspended: '已暂停'
  }
  return map[status || ''] || ''
}

const getTodoIcon = (type: string) => {
  const map: Record<string, string> = {
    order: '📋',
    activity: '🎉',
    billing: '💰',
    review: '⭐'
  }
  return map[type] || '📌'
}

const loadDashboard = async () => {
  try {
    const data = await get<{
      merchant: MerchantInfo
      stats: Stats
      todos: TodoItem[]
      recentOrders: OrderItem[]
    }>('/v1/merchant/dashboard', { dateRange: dateRange.value })
    
    merchantInfo.value = data.merchant
    stats.value = data.stats
    todoList.value = data.todos || []
    recentOrders.value = data.recentOrders || []
  } catch (e) {
    console.error('加载仪表盘失败', e)
  }
}

const handleTodo = (item: TodoItem) => {
  if (item.targetUrl) {
    uni.navigateTo({ url: item.targetUrl })
  }
}

const goActivityList = () => {
  uni.navigateTo({ url: '/pages/admin/activity/list' })
}

const goVerification = () => {
  uni.navigateTo({ url: '/pages/merchant/verification/scan' })
}

const goMembers = () => {
  uni.navigateTo({ url: '/pages/admin/member/stats' })
}

const goOrders = () => {
  uni.navigateTo({ url: '/pages/admin/order/list' })
}

const goBilling = () => {
  uni.navigateTo({ url: '/pages/admin/billing/index' })
}

const goEmployeeBind = () => {
  uni.navigateTo({ url: '/pages/merchant/employee/bind' })
}

const goSettings = () => {
  uni.navigateTo({ url: '/pages/admin/settings' })
}

watch(dateRange, () => {
  loadDashboard()
})

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss">
.dashboard-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.merchant-header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  padding: 40rpx 24rpx;
  
  .logo {
    width: 100rpx;
    height: 100rpx;
    border-radius: 16rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
  }
  
  .info {
    flex: 1;
    margin-left: 20rpx;
    
    .name {
      display: block;
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
    }
    
    .status-badge {
      display: inline-block;
      margin-top: 8rpx;
      padding: 4rpx 16rpx;
      border-radius: 16rpx;
      font-size: 22rpx;
      
      &.active {
        background: rgba(255, 255, 255, 0.3);
        color: #fff;
      }
      
      &.pending {
        background: #ffc107;
        color: #333;
      }
      
      &.suspended {
        background: #f44336;
        color: #fff;
      }
    }
  }
  
  .settings-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
  }
}

.stats-section {
  background: #fff;
  margin: -20rpx 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;
  z-index: 1;
  
  .stats-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
  
  .date-tabs {
    display: flex;
    margin-top: 16rpx;
    
    text {
      padding: 8rpx 24rpx;
      font-size: 26rpx;
      color: #666;
      border-radius: 20rpx;
      margin-right: 16rpx;
      
      &.active {
        background: #ff6b35;
        color: #fff;
      }
    }
  }
  
  .stats-grid {
    display: flex;
    flex-wrap: wrap;
    margin-top: 24rpx;
    
    .stat-card {
      width: 50%;
      padding: 20rpx;
      text-align: center;
      box-sizing: border-box;
      
      .num {
        display: block;
        font-size: 40rpx;
        font-weight: bold;
        color: #333;
      }
      
      .label {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
      
      .trend {
        display: block;
        font-size: 22rpx;
        color: #f44336;
        margin-top: 4rpx;
        
        &.up {
          color: #4caf50;
        }
      }
    }
  }
}

.quick-entry {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  
  .entry-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .entry-grid {
    display: flex;
    flex-wrap: wrap;
    
    .entry-item {
      width: 25%;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 0;
      
      .icon-wrap {
        width: 80rpx;
        height: 80rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36rpx;
        
        &.activity { background: #fff3e0; }
        &.scan { background: #e3f2fd; }
        &.member { background: #fce4ec; }
        &.order { background: #e8f5e9; }
        &.billing { background: #fff8e1; }
        &.service { background: #f3e5f5; }
        &.employee { background: #e0f7fa; }
        &.settings { background: #f5f5f5; }
      }
      
      .label {
        font-size: 24rpx;
        color: #666;
        margin-top: 12rpx;
      }
    }
  }
}

.todo-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
  
  .count {
    padding: 4rpx 16rpx;
    background: #ff4757;
    color: #fff;
    font-size: 22rpx;
    border-radius: 16rpx;
  }
  
  .more {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #999;
    
    .arrow {
      margin-left: 8rpx;
    }
  }
}

.todo-list {
  .todo-item {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .todo-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      
      &.order { background: #e8f5e9; }
      &.activity { background: #fff3e0; }
      &.billing { background: #fff8e1; }
      &.review { background: #fce4ec; }
    }
    
    .todo-content {
      flex: 1;
      margin-left: 16rpx;
      
      .title {
        display: block;
        font-size: 28rpx;
        color: #333;
      }
      
      .desc {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
    
    .arrow {
      font-size: 28rpx;
      color: #ccc;
    }
  }
}

.recent-orders {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.order-list {
  .order-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .left {
      .title {
        display: block;
        font-size: 28rpx;
        color: #333;
      }
      
      .time {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
    
    .right {
      text-align: right;
      
      .amount {
        display: block;
        font-size: 30rpx;
        font-weight: bold;
        color: #4caf50;
      }
      
      .status {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
  
  .empty-order {
    text-align: center;
    padding: 40rpx;
    color: #999;
    font-size: 26rpx;
  }
}
</style>
