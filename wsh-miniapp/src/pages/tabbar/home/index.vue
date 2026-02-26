<template>
  <view class="index-page cyber-theme">
    <!-- 城市选择器 -->
    <CitySelector 
      v-model:visible="showCitySelector" 
      @select="onCitySelected"
    />
    
    <!-- AI悬浮按钮 -->
    <AiFab />
    
    <!-- 扫描线动效 -->
    <view class="scan-line"></view>
    
    <!-- 未登录时显示公开活动 -->
    <template v-if="!userStore.isLoggedIn">
      <view class="guest-header">
        <text class="title">同城活动广场</text>
        <view class="city-select" @tap="openCitySelector">
          <text>{{ appStore.currentCityName }}</text>
          <text class="arrow">▼</text>
        </view>
      </view>
      
      <view class="login-banner" @tap="goLogin">
        <view class="banner-glow"></view>
        <text>登录查看您的权益资产</text>
        <text class="btn">立即登录 →</text>
      </view>
    </template>
    
    <!-- 已登录时显示权益总览 -->
    <template v-else>
      <view class="equity-header">
        <view class="header-bg"></view>
        <view class="user-info">
          <image class="avatar" :src="userStore.avatarUrl || '/static/default-avatar.png'" />
          <text class="greeting">Hi, {{ userStore.nickname || '会员' }}</text>
        </view>
        <view class="city-select" @tap="openCitySelector">
          <text>{{ appStore.currentCityName }}</text>
          <text class="arrow">▼</text>
        </view>
      </view>
      
      <!-- 权益卡片 -->
      <view class="equity-card" @tap="goEquitySummary">
        <view class="card-glow"></view>
        <view class="card-header">
          <text class="title">我的权益资产</text>
          <text class="action">查看详情 →</text>
        </view>
        <view class="total-value">
          <text class="symbol">¥</text>
          <text class="amount">{{ formatMoney(equityStore.totalValue) }}</text>
        </view>
        <view class="value-detail">
          <view class="item">
            <text class="label">积分价值</text>
            <text class="value">¥{{ formatMoney(equityStore.summary?.totalPointsValue || 0) }}</text>
          </view>
          <view class="item">
            <text class="label">储值余额</text>
            <text class="value">¥{{ formatMoney(equityStore.summary?.totalBalance || 0) }}</text>
          </view>
          <view class="item">
            <text class="label">优惠券</text>
            <text class="value">¥{{ formatMoney(equityStore.summary?.totalVoucherValue || 0) }}</text>
          </view>
        </view>
      </view>
      
      <!-- 过期提醒 -->
      <view class="expiring-banner" v-if="equityStore.hasExpiring" @tap="goExpiring">
        <text class="icon">⚠️</text>
        <text class="text">有 {{ equityStore.expiringCount }} 项权益即将过期</text>
        <text class="action">查看 →</text>
      </view>
    </template>
    
    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="entry-item" @tap="goNearby">
        <view class="icon-wrap nearby">📍</view>
        <text>附近优惠</text>
      </view>
      <view class="entry-item" @tap="goMembers">
        <view class="icon-wrap member">👤</view>
        <text>我的会员</text>
      </view>
      <view class="entry-item" @tap="goOrders">
        <view class="icon-wrap order">📋</view>
        <text>我的订单</text>
      </view>
      <view class="entry-item" @tap="goGroupBuy">
        <view class="icon-wrap groupbuy">🎯</view>
        <text>我的拼团</text>
      </view>
    </view>
    
    <!-- 推荐活动 -->
    <view class="section">
      <view class="section-header">
        <text class="title">推荐活动</text>
        <text class="more" @tap="goActivityList">更多 →</text>
      </view>
      <view class="activity-list">
        <view class="activity-card" v-for="item in recommendActivities" :key="item.activityId" @tap="goActivityDetail(item.activityId)">
          <image class="cover" :src="item.coverImage || '/static/default-activity.png'" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.activityName }}</text>
            <text class="merchant">{{ item.merchantName }}</text>
            <view class="price-row">
              <text class="price">¥{{ item.sellingPrice || 0 }}</text>
              <text class="original" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
            </view>
          </view>
        </view>
        
        <view class="empty-tip" v-if="recommendActivities.length === 0">
          <text>暂无推荐活动</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { useEquityStore } from '@/store/equity'
import { useAppStore } from '@/store/app'
import { getPublicActivities, type PublicActivity } from '@/api/public'
import CitySelector from '@/components/CitySelector.vue'
import AiFab from '@/components/AiFab.vue'
import type { CityItem } from '@/api/city'

const userStore = useUserStore()
const equityStore = useEquityStore()
const appStore = useAppStore()

const recommendActivities = ref<PublicActivity[]>([])
const showCitySelector = ref(false)

onMounted(() => {
  // 初始化城市
  appStore.initCity()
})

onShow(() => {
  loadData()
})

async function loadData() {
  // 加载推荐活动
  try {
    const res = await getPublicActivities(appStore.currentCityName)
    // 取各类型前2个作为推荐
    recommendActivities.value = [
      ...res.voucherActivities.slice(0, 2),
      ...res.depositActivities.slice(0, 2),
      ...res.groupActivities.slice(0, 2)
    ].slice(0, 6)
  } catch (err) {
    console.error('加载活动失败', err)
  }
  
  // 已登录则加载权益
  if (userStore.isLoggedIn) {
    equityStore.fetchSummary()
    equityStore.fetchExpiring()
  }
}

function formatMoney(value: number): string {
  return value.toFixed(2)
}

function openCitySelector() {
  showCitySelector.value = true
}

function onCitySelected(city: CityItem) {
  // 城市切换后重新加载数据
  loadData()
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goEquitySummary() {
  uni.navigateTo({ url: '/pages/consumer/equity/summary' })
}

function goExpiring() {
  uni.navigateTo({ url: '/pages/consumer/equity/expiring' })
}

function goNearby() {
  uni.navigateTo({ url: '/pages/consumer/activity/nearby' })
}

function goMembers() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/member/list' })
}

function goOrders() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/order/list' })
}

function goGroupBuy() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/consumer/groupbuy/list' })
}

function goActivityList() {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

function goActivityDetail(id: number) {
  uni.navigateTo({ url: `/pages/consumer/activity/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
/* 赛博朋克配色 */
$cyber-primary: #00f2ff;
$cyber-purple: #7928ca;
$cyber-pink: #ff0080;
$cyber-orange: #ff4500;
$dark-bg: #0a0a0f;
$dark-card: #1a1a25;
$dark-border: rgba(255, 255, 255, 0.1);

.index-page.cyber-theme {
  min-height: 100vh;
  background: linear-gradient(180deg, $dark-bg 0%, #12121a 100%);
  padding-bottom: 40rpx;
  position: relative;
  overflow: hidden;
}

/* 扫描线动效 */
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, $cyber-primary, transparent);
  opacity: 0.5;
  animation: scan 4s linear infinite;
  pointer-events: none;
  z-index: 100;
}

@keyframes scan {
  0% { transform: translateY(0); }
  100% { transform: translateY(100vh); }
}

.guest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid $dark-border;
  
  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 0 20rpx rgba($cyber-primary, 0.5);
  }
  
  .city-select {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 28rpx;
    color: $cyber-primary;
    padding: 12rpx 20rpx;
    border: 1rpx solid rgba($cyber-primary, 0.3);
    border-radius: 8rpx;
    
    .arrow {
      font-size: 20rpx;
    }
  }
}

.login-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24rpx;
  padding: 28rpx 32rpx;
  background: linear-gradient(135deg, rgba($cyber-primary, 0.2) 0%, rgba($cyber-purple, 0.2) 100%);
  border: 1rpx solid rgba($cyber-primary, 0.3);
  border-radius: 16rpx;
  color: #fff;
  position: relative;
  overflow: hidden;
  
  .banner-glow {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: linear-gradient(90deg, $cyber-primary, $cyber-purple);
  }
  
  .btn {
    font-weight: 500;
    color: $cyber-primary;
  }
}

.equity-header {
  padding: 32rpx;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba($cyber-purple, 0.3) 0%, rgba($cyber-primary, 0.1) 100%);
    z-index: 0;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    position: relative;
    z-index: 1;
    
    .avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      border: 3rpx solid $cyber-primary;
      box-shadow: 0 0 20rpx rgba($cyber-primary, 0.5);
    }
    
    .greeting {
      font-size: 32rpx;
      color: #fff;
      font-weight: 500;
    }
  }
  
  .city-select {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 26rpx;
    color: $cyber-primary;
    position: relative;
    z-index: 1;
    
    .arrow {
      font-size: 18rpx;
    }
  }
}

.equity-card {
  margin: 24rpx;
  padding: 32rpx;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20rpx);
  border: 1rpx solid $dark-border;
  border-radius: 20rpx;
  position: relative;
  overflow: hidden;
  
  .card-glow {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: linear-gradient(90deg, $cyber-primary, $cyber-purple, $cyber-pink);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .title {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.7);
      font-weight: 500;
    }
    
    .action {
      font-size: 24rpx;
      color: $cyber-primary;
    }
  }
  
  .total-value {
    display: flex;
    align-items: baseline;
    margin-bottom: 24rpx;
    
    .symbol {
      font-size: 32rpx;
      color: $cyber-primary;
    }
    
    .amount {
      font-size: 64rpx;
      font-weight: 700;
      color: #fff;
      text-shadow: 0 0 30rpx rgba($cyber-primary, 0.5);
    }
  }
  
  .value-detail {
    display: flex;
    justify-content: space-between;
    padding-top: 24rpx;
    border-top: 1rpx solid $dark-border;
    
    .item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      
      .label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.4);
      }
      
      .value {
        font-size: 28rpx;
        color: #fff;
        font-weight: 500;
      }
    }
  }
}

.expiring-banner {
  display: flex;
  align-items: center;
  margin: 0 24rpx 24rpx;
  padding: 20rpx 24rpx;
  background: rgba($cyber-orange, 0.15);
  border-radius: 12rpx;
  border: 1rpx solid rgba($cyber-orange, 0.3);
  
  .icon {
    font-size: 32rpx;
    margin-right: 12rpx;
  }
  
  .text {
    flex: 1;
    font-size: 26rpx;
    color: $cyber-orange;
  }
  
  .action {
    font-size: 26rpx;
    color: $cyber-orange;
    font-weight: 500;
  }
}

.quick-entry {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 24rpx;
  margin: 0 24rpx 24rpx;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20rpx);
  border: 1rpx solid $dark-border;
  border-radius: 20rpx;
  
  .entry-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    
    .icon-wrap {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      font-size: 36rpx;
      border: 1rpx solid $dark-border;
      
      &.nearby { 
        background: rgba($cyber-primary, 0.15);
        border-color: rgba($cyber-primary, 0.3);
      }
      &.member { 
        background: rgba($cyber-purple, 0.15);
        border-color: rgba($cyber-purple, 0.3);
      }
      &.order { 
        background: rgba(#00ff88, 0.15);
        border-color: rgba(#00ff88, 0.3);
      }
      &.groupbuy { 
        background: rgba($cyber-pink, 0.15);
        border-color: rgba($cyber-pink, 0.3);
      }
    }
    
    text:last-child {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

.section {
  margin: 0 24rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .more {
      font-size: 26rpx;
      color: $cyber-primary;
    }
  }
}

.activity-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  
  .activity-card {
    width: calc(50% - 10rpx);
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10rpx);
    border: 1rpx solid $dark-border;
    border-radius: 16rpx;
    overflow: hidden;
    
    .cover {
      width: 100%;
      height: 200rpx;
    }
    
    .info {
      padding: 16rpx;
      
      .name {
        font-size: 28rpx;
        color: #fff;
        font-weight: 500;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .merchant {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.5);
        margin-top: 8rpx;
      }
      
      .price-row {
        display: flex;
        align-items: baseline;
        gap: 12rpx;
        margin-top: 12rpx;
        
        .price {
          font-size: 32rpx;
          color: $cyber-pink;
          font-weight: 600;
          text-shadow: 0 0 10rpx rgba($cyber-pink, 0.5);
        }
        
        .original {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.4);
          text-decoration: line-through;
        }
      }
    }
  }
  
  .empty-tip {
    width: 100%;
    padding: 60rpx;
    text-align: center;
    color: rgba(255, 255, 255, 0.4);
    font-size: 28rpx;
  }
}
</style>
