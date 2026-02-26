<template>
  <view class="mine-page">
    <!-- AI助手悬浮按钮 -->
    <view class="ai-float-btn" @tap="goAiChat">
      <text class="ai-icon">🤖</text>
      <text class="ai-text">AI助手</text>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info" v-if="userStore.isLoggedIn">
        <image class="avatar" :src="userStore.avatarUrl || '/static/default-avatar.png'" />
        <view class="info">
          <text class="nickname">{{ userStore.nickname || '微生活用户' }}</text>
          <text class="phone" v-if="userStore.phone">{{ maskPhone(userStore.phone) }}</text>
        </view>
      </view>
      <view class="user-info guest" v-else @tap="goLogin">
        <image class="avatar" src="/static/default-avatar.png" />
        <view class="info">
          <text class="nickname">点击登录</text>
          <text class="tips">登录查看您的权益资产</text>
        </view>
      </view>
      
      <!-- 商户入口（商户角色显示） -->
      <view class="merchant-entry" v-if="userStore.isMerchant" @tap="goMerchantAdmin">
        <text>进入商户中心</text>
        <text class="arrow">→</text>
      </view>
    </view>
    
    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-title">订单与券</view>
      <view class="menu-grid">
        <view class="menu-item" @tap="goOrders()">
          <text class="icon">📋</text>
          <text>全部订单</text>
        </view>
        <view class="menu-item" @tap="goOrders(0)">
          <text class="icon">💳</text>
          <text>待付款</text>
        </view>
        <view class="menu-item" @tap="goVouchers()">
          <text class="icon">🎫</text>
          <text>我的券包</text>
        </view>
        <view class="menu-item" @tap="goInvoices">
          <text class="icon">🧾</text>
          <text>我的发票</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-title">会员权益</view>
      <view class="menu-list">
        <view class="menu-row" @tap="goMembers">
          <text class="icon">👤</text>
          <text class="label">我的会员</text>
          <text class="desc">查看各商户会员信息</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" @tap="goEquitySummary">
          <text class="icon">💰</text>
          <text class="label">权益总览</text>
          <text class="desc">积分、储值、优惠券汇总</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" @tap="goExpiring">
          <text class="icon">⏰</text>
          <text class="label">即将过期</text>
          <text class="desc">{{ expiringCount }}项权益即将过期</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" @tap="goMessages">
          <text class="icon">🔔</text>
          <text class="label">消息中心</text>
          <text class="desc">权益提醒消息</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-title">其他</view>
      <view class="menu-list">
        <view class="menu-row highlight" @tap="goAiChat">
          <text class="icon">🤖</text>
          <text class="label">AI智能助手</text>
          <view class="new-tag">NEW</view>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" @tap="goSettings">
          <text class="icon">⚙️</text>
          <text class="label">设置</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" @tap="goAbout">
          <text class="icon">ℹ️</text>
          <text class="label">关于我们</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-row" v-if="userStore.isLoggedIn" @tap="handleLogout">
          <text class="icon">🚪</text>
          <text class="label">退出登录</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { useEquityStore } from '@/store/equity'

const userStore = useUserStore()
const equityStore = useEquityStore()

const expiringCount = computed(() => equityStore.expiringCount)

onShow(() => {
  if (userStore.isLoggedIn) {
    equityStore.fetchExpiring()
  }
})

function maskPhone(phone: string): string {
  if (!phone || phone.length < 11) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goMerchantAdmin() {
  uni.navigateTo({ url: '/pages/admin/dashboard' })
}

function goOrders(status?: number) {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  const url = status !== undefined 
    ? `/pages/consumer/order/list?status=${status}`
    : '/pages/consumer/order/list'
  uni.navigateTo({ url })
}

function goVouchers(status?: number) {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  const url = status !== undefined 
    ? `/pages/consumer/voucher/list?status=${status}`
    : '/pages/consumer/voucher/list'
  uni.navigateTo({ url })
}

function goInvoices() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/invoice/list' })
}

function goMembers() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/member/list' })
}

function goEquitySummary() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/equity/summary' })
}

function goExpiring() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/equity/expiring' })
}

function goMessages() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/message/list' })
}

function goAiChat() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/ai/chat' })
}

function goSettings() {
  uni.showToast({ title: '设置页面开发中', icon: 'none' })
}

function goAbout() {
  uni.showToast({ title: '关于我们页面开发中', icon: 'none' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

// AI悬浮按钮
.ai-float-btn {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.5);
  z-index: 100;
  animation: pulse 2s infinite;
  
  .ai-icon {
    font-size: 36rpx;
  }
  
  .ai-text {
    font-size: 20rpx;
    color: #fff;
    margin-top: 4rpx;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.5);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.7);
  }
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 32rpx 40rpx;
  margin-bottom: 24rpx;
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    &.guest {
      cursor: pointer;
    }
    
    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255,255,255,0.3);
    }
    
    .info {
      display: flex;
      flex-direction: column;
      gap: 8rpx;
      
      .nickname {
        font-size: 36rpx;
        font-weight: 600;
        color: #fff;
      }
      
      .phone, .tips {
        font-size: 26rpx;
        color: rgba(255,255,255,0.8);
      }
    }
  }
  
  .merchant-entry {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 32rpx;
    padding: 20rpx 24rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 12rpx;
    color: #fff;
    font-size: 28rpx;
    
    .arrow {
      font-size: 32rpx;
    }
  }
}

.menu-section {
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  
  .menu-title {
    padding: 24rpx 24rpx 16rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 16rpx 0 24rpx;
  
  .menu-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    
    .icon {
      font-size: 48rpx;
    }
    
    text:last-child {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.menu-list {
  .menu-row {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    &.highlight {
      background: linear-gradient(90deg, rgba(102, 126, 234, 0.08) 0%, transparent 100%);
    }
    
    .icon {
      font-size: 40rpx;
      margin-right: 20rpx;
    }
    
    .label {
      font-size: 30rpx;
      color: #333;
      margin-right: 16rpx;
    }
    
    .desc {
      flex: 1;
      font-size: 26rpx;
      color: #999;
      text-align: right;
      margin-right: 12rpx;
    }
    
    .new-tag {
      flex: 1;
      font-size: 20rpx;
      color: #fff;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      padding: 4rpx 12rpx;
      border-radius: 16rpx;
      margin-right: 12rpx;
      width: fit-content;
    }
    
    .arrow {
      font-size: 32rpx;
      color: #ccc;
    }
  }
}
</style>
