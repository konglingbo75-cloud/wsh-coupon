<template>
  <view class="test-page">
    <view class="header">
      <text class="title">微生活券吧</text>
      <text class="subtitle">开发测试入口</text>
    </view>

    <!-- 主包页面 -->
    <view class="section">
      <view class="section-title">主包页面</view>
      <view class="page-grid">
        <view class="page-item" @click="goTo('/pages/login/index')">
          <text class="icon">🔐</text>
          <text class="name">登录页</text>
        </view>
        <view class="page-item" @click="switchTo('/pages/tabbar/home/index')">
          <text class="icon">🏠</text>
          <text class="name">首页</text>
        </view>
        <view class="page-item" @click="switchTo('/pages/tabbar/activity/index')">
          <text class="icon">🎉</text>
          <text class="name">活动广场</text>
        </view>
        <view class="page-item" @click="switchTo('/pages/tabbar/mine/index')">
          <text class="icon">👤</text>
          <text class="name">我的</text>
        </view>
      </view>
    </view>

    <!-- 消费者分包 -->
    <view class="section">
      <view class="section-title">消费者分包 (Consumer)</view>
      <view class="page-grid">
        <view class="page-item" @click="goTo('/pages/consumer/equity/summary')">
          <text class="icon">💎</text>
          <text class="name">权益总览</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/equity/expiring')">
          <text class="icon">⏰</text>
          <text class="name">即将过期</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/member/list')">
          <text class="icon">🏪</text>
          <text class="name">会员列表</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/member/detail?merchantId=test')">
          <text class="icon">🏬</text>
          <text class="name">会员详情</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/activity/detail?id=test')">
          <text class="icon">📋</text>
          <text class="name">活动详情</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/activity/nearby')">
          <text class="icon">📍</text>
          <text class="name">附近优惠</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/order/confirm?activityId=test')">
          <text class="icon">✅</text>
          <text class="name">确认订单</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/order/list')">
          <text class="icon">📦</text>
          <text class="name">订单列表</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/order/detail?id=test')">
          <text class="icon">📄</text>
          <text class="name">订单详情</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/voucher/list')">
          <text class="icon">🎫</text>
          <text class="name">券列表</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/voucher/detail?id=test')">
          <text class="icon">🎟️</text>
          <text class="name">券详情</text>
        </view>
        <view class="page-item" @click="goTo('/pages/consumer/message/list')">
          <text class="icon">📬</text>
          <text class="name">消息中心</text>
        </view>
      </view>
    </view>

    <!-- 商家核销分包 -->
    <view class="section">
      <view class="section-title">商家核销分包 (Merchant)</view>
      <view class="page-grid">
        <view class="page-item" @click="goTo('/pages/merchant/verification/scan')">
          <text class="icon">📷</text>
          <text class="name">扫码核销</text>
        </view>
        <view class="page-item" @click="goTo('/pages/merchant/verification/record')">
          <text class="icon">📊</text>
          <text class="name">核销记录</text>
        </view>
        <view class="page-item" @click="goTo('/pages/merchant/employee/bind')">
          <text class="icon">🔗</text>
          <text class="name">员工绑定</text>
        </view>
      </view>
    </view>

    <!-- 商户管理分包 -->
    <view class="section">
      <view class="section-title">商户管理分包 (Admin)</view>
      <view class="page-grid">
        <view class="page-item" @click="goTo('/pages/admin/dashboard')">
          <text class="icon">📈</text>
          <text class="name">商户中心</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/activity/list')">
          <text class="icon">🎪</text>
          <text class="name">活动管理</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/member/stats')">
          <text class="icon">👥</text>
          <text class="name">会员统计</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/billing/onboarding')">
          <text class="icon">💰</text>
          <text class="name">入驻费</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/billing/service-fee')">
          <text class="icon">📑</text>
          <text class="name">服务费</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/order/list')">
          <text class="icon">🛒</text>
          <text class="name">订单管理</text>
        </view>
        <view class="page-item" @click="goTo('/pages/admin/settings')">
          <text class="icon">⚙️</text>
          <text class="name">设置</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="section">
      <view class="section-title">快捷操作</view>
      <view class="action-list">
        <button class="action-btn" @click="mockLogin">模拟登录 (消费者)</button>
        <button class="action-btn merchant" @click="mockMerchantLogin">模拟登录 (商户)</button>
        <button class="action-btn danger" @click="clearStorage">清除缓存</button>
      </view>
    </view>

    <!-- 状态信息 -->
    <view class="section status-section">
      <view class="section-title">当前状态</view>
      <view class="status-list">
        <view class="status-item">
          <text class="label">登录状态:</text>
          <text class="value" :class="{ success: isLoggedIn }">{{ isLoggedIn ? '已登录' : '未登录' }}</text>
        </view>
        <view class="status-item">
          <text class="label">用户角色:</text>
          <text class="value">{{ userRole || '无' }}</text>
        </view>
        <view class="status-item">
          <text class="label">Token:</text>
          <text class="value token">{{ tokenPreview }}</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <text>WSH Mini-App v1.0.0 | 开发测试版</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { setToken, clearToken } from '@/api/request'

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userRole = computed(() => userStore.role)
const tokenPreview = computed(() => {
  const token = userStore.token
  if (!token) return '无'
  if (token.length > 20) {
    return token.slice(0, 10) + '...' + token.slice(-6)
  }
  return token
})

const goTo = (url: string) => {
  uni.navigateTo({ url })
}

const switchTo = (url: string) => {
  uni.switchTab({ url })
}

const mockLogin = () => {
  // 模拟消费者登录（与后端 local profile Mock Token 对齐）
  const mockToken = 'mock_user_token_100001'
  setToken(mockToken)
  userStore.setUserInfo({
    token: mockToken,
    userId: '100001',
    openid: 'mock_openid_001',
    nickname: '测试用户',
    avatarUrl: '',
    phone: '13800138000',
    role: 'consumer'
  })
  uni.showToast({ title: '已模拟登录(消费者)', icon: 'success' })
}

const mockMerchantLogin = () => {
  // 模拟商户登录（与后端 local profile Mock Token 对齐）
  const mockToken = 'mock_merchant_token_200001'
  setToken(mockToken)
  userStore.setUserInfo({
    token: mockToken,
    userId: '200001',
    openid: 'mock_openid_002',
    nickname: '测试商户（老王烤鱼）',
    avatarUrl: '',
    phone: '13900139000',
    role: 'merchant'
  })
  uni.showToast({ title: '已模拟登录(商户)', icon: 'success' })
}

const clearStorage = () => {
  uni.showModal({
    title: '确认清除',
    content: '将清除所有本地缓存数据，包括登录状态',
    success: (res) => {
      if (res.confirm) {
        clearToken()
        userStore.logout()
        uni.clearStorageSync()
        uni.showToast({ title: '已清除', icon: 'success' })
      }
    }
  })
}

// 页面显示时刷新状态
onShow(() => {
  userStore.checkLoginStatus()
})
</script>

<style lang="scss">
.test-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24rpx;
  padding-bottom: 60rpx;
}

.header {
  text-align: center;
  padding: 40rpx 0;
  color: #fff;
  
  .title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
  }
  
  .subtitle {
    display: block;
    font-size: 26rpx;
    opacity: 0.8;
    margin-top: 12rpx;
  }
}

.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.page-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -8rpx;
}

.page-item {
  width: 25%;
  padding: 8rpx;
  box-sizing: border-box;
  
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 8rpx;
  
  .icon {
    font-size: 40rpx;
    margin-bottom: 8rpx;
  }
  
  .name {
    font-size: 22rpx;
    color: #666;
    text-align: center;
    line-height: 1.3;
  }
  
  &:active {
    background: #f5f5f5;
    border-radius: 12rpx;
  }
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.action-btn {
  padding: 20rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
  
  &.merchant {
    background: linear-gradient(135deg, #ff6b35, #f7931e);
  }
  
  &.danger {
    background: #f5f5f5;
    color: #f44336;
  }
}

.status-section {
  .status-list {
    .status-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12rpx 0;
      border-bottom: 1rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        font-size: 26rpx;
        color: #999;
      }
      
      .value {
        font-size: 26rpx;
        color: #333;
        
        &.success {
          color: #4caf50;
        }
        
        &.token {
          font-family: monospace;
          font-size: 22rpx;
          color: #666;
          max-width: 300rpx;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
}

.footer {
  text-align: center;
  padding: 32rpx 0;
  
  text {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>
