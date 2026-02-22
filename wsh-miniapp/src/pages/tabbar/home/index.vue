<template>
  <view class="index-page">
    <!-- 未登录时显示公开活动 -->
    <template v-if="!userStore.isLoggedIn">
      <view class="guest-header">
        <text class="title">同城活动广场</text>
        <view class="city-select" @tap="selectCity">
          <text>{{ appStore.currentCity }}</text>
          <text class="arrow">▼</text>
        </view>
      </view>
      
      <view class="login-banner" @tap="goLogin">
        <text>登录查看您的权益资产</text>
        <text class="btn">立即登录 →</text>
      </view>
    </template>
    
    <!-- 已登录时显示权益总览 -->
    <template v-else>
      <view class="equity-header">
        <view class="user-info">
          <image class="avatar" :src="userStore.avatarUrl || '/static/default-avatar.png'" />
          <text class="greeting">Hi, {{ userStore.nickname || '会员' }}</text>
        </view>
      </view>
      
      <!-- 权益卡片 -->
      <view class="equity-card" @tap="goEquitySummary">
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
      <view class="entry-item" @tap="goVouchers">
        <view class="icon-wrap voucher">🎫</view>
        <text>我的券包</text>
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

const userStore = useUserStore()
const equityStore = useEquityStore()
const appStore = useAppStore()

const recommendActivities = ref<PublicActivity[]>([])

onShow(() => {
  loadData()
})

async function loadData() {
  // 加载推荐活动
  try {
    const res = await getPublicActivities(appStore.currentCity)
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

function selectCity() {
  // TODO: 城市选择
  uni.showToast({ title: '城市选择功能开发中', icon: 'none' })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goEquitySummary() {
  uni.navigateTo({ url: '/subPackages/consumer/equity/summary' })
}

function goExpiring() {
  uni.navigateTo({ url: '/subPackages/consumer/equity/expiring' })
}

function goNearby() {
  uni.navigateTo({ url: '/subPackages/consumer/activity/nearby' })
}

function goMembers() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/subPackages/consumer/member/list' })
}

function goOrders() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/subPackages/consumer/order/list' })
}

function goVouchers() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/subPackages/consumer/voucher/list' })
}

function goActivityList() {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

function goActivityDetail(id: number) {
  uni.navigateTo({ url: `/subPackages/consumer/activity/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.index-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

.guest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: #fff;
  
  .title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .city-select {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 28rpx;
    color: #666;
    
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
  padding: 24rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  color: #fff;
  
  .btn {
    font-weight: 500;
  }
}

.equity-header {
  padding: 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    
    .avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255,255,255,0.3);
    }
    
    .greeting {
      font-size: 32rpx;
      color: #fff;
      font-weight: 500;
    }
  }
}

.equity-card {
  margin: -40rpx 24rpx 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .title {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
    
    .action {
      font-size: 24rpx;
      color: #667eea;
    }
  }
  
  .total-value {
    display: flex;
    align-items: baseline;
    margin-bottom: 24rpx;
    
    .symbol {
      font-size: 32rpx;
      color: #333;
    }
    
    .amount {
      font-size: 64rpx;
      font-weight: 700;
      color: #333;
    }
  }
  
  .value-detail {
    display: flex;
    justify-content: space-between;
    padding-top: 24rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      
      .label {
        font-size: 24rpx;
        color: #999;
      }
      
      .value {
        font-size: 28rpx;
        color: #333;
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
  background: #fff8e6;
  border-radius: 12rpx;
  border: 1rpx solid #ffe58f;
  
  .icon {
    font-size: 32rpx;
    margin-right: 12rpx;
  }
  
  .text {
    flex: 1;
    font-size: 26rpx;
    color: #d48806;
  }
  
  .action {
    font-size: 26rpx;
    color: #d48806;
    font-weight: 500;
  }
}

.quick-entry {
  display: flex;
  justify-content: space-around;
  padding: 32rpx 24rpx;
  margin: 0 24rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  
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
      
      &.nearby { background: #e3f2fd; }
      &.member { background: #f3e5f5; }
      &.order { background: #e8f5e9; }
      &.voucher { background: #fff3e0; }
    }
    
    text:last-child {
      font-size: 24rpx;
      color: #666;
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
      color: #333;
    }
    
    .more {
      font-size: 26rpx;
      color: #999;
    }
  }
}

.activity-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  
  .activity-card {
    width: calc(50% - 10rpx);
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    
    .cover {
      width: 100%;
      height: 200rpx;
    }
    
    .info {
      padding: 16rpx;
      
      .name {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .merchant {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
      
      .price-row {
        display: flex;
        align-items: baseline;
        gap: 12rpx;
        margin-top: 12rpx;
        
        .price {
          font-size: 32rpx;
          color: #e53935;
          font-weight: 600;
        }
        
        .original {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
        }
      }
    }
  }
  
  .empty-tip {
    width: 100%;
    padding: 60rpx;
    text-align: center;
    color: #999;
    font-size: 28rpx;
  }
}
</style>
