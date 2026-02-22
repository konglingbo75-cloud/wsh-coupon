<template>
  <view class="expiring-page">
    <!-- 筛选区 -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: currentDays === 3 }" @click="setDays(3)">
        <text>3天内</text>
      </view>
      <view class="filter-item" :class="{ active: currentDays === 7 }" @click="setDays(7)">
        <text>7天内</text>
      </view>
      <view class="filter-item" :class="{ active: currentDays === 30 }" @click="setDays(30)">
        <text>30天内</text>
      </view>
    </view>

    <!-- 过期列表 -->
    <view class="expiring-list">
      <view v-for="item in expiringList" :key="item.id" class="expiring-card" @click="goDetail(item)">
        <view class="card-header">
          <view class="merchant-info">
            <image class="logo" :src="item.merchantLogo || '/static/default-merchant.png'" mode="aspectFill" />
            <text class="name">{{ item.merchantName }}</text>
          </view>
          <view class="expire-badge" :class="getExpireClass(item.daysLeft)">
            {{ item.daysLeft }}天后过期
          </view>
        </view>
        <view class="card-body">
          <view class="equity-info">
            <view class="type-tag" :class="item.type">{{ getTypeName(item.type) }}</view>
            <text class="equity-name">{{ item.equityName }}</text>
          </view>
          <view class="value-section">
            <text class="value">¥{{ item.value }}</text>
            <text class="expire-date">{{ item.expireDate }}到期</text>
          </view>
        </view>
        <view class="card-footer">
          <text class="hint">点击查看详情并使用</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && expiringList.length === 0" class="empty-state">
      <image src="/static/empty-expiring.png" mode="aspectFit" class="empty-icon" />
      <text class="empty-text">暂无即将过期的权益</text>
      <text class="empty-hint">您的权益都还有效哦~</text>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getExpiringEquities } from '@/api/equity'

interface ExpiringItem {
  id: string
  equityName: string
  merchantName: string
  merchantLogo: string
  value: number
  type: 'voucher' | 'deposit' | 'points'
  daysLeft: number
  expireDate: string
}

const loading = ref(true)
const currentDays = ref(7)
const expiringList = ref<ExpiringItem[]>([])

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    voucher: '优惠券',
    deposit: '储值',
    points: '积分'
  }
  return map[type] || type
}

const getExpireClass = (days: number) => {
  if (days <= 1) return 'urgent'
  if (days <= 3) return 'warning'
  return 'normal'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExpiringEquities({ days: currentDays.value, limit: 50 })
    if (res.code === 0) {
      expiringList.value = res.data.list || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const setDays = (days: number) => {
  currentDays.value = days
}

const goDetail = (item: ExpiringItem) => {
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${item.id}` })
}

watch(currentDays, () => {
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<style lang="scss">
.expiring-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 24rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .filter-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    margin: 0 12rpx;
    border-radius: 32rpx;
    font-size: 28rpx;
    color: #666;
    background: #f5f5f5;
    
    &.active {
      background: #ff6b35;
      color: #fff;
    }
  }
}

.expiring-list {
  padding: 24rpx;
}

.expiring-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .merchant-info {
      display: flex;
      align-items: center;
      
      .logo {
        width: 48rpx;
        height: 48rpx;
        border-radius: 8rpx;
      }
      
      .name {
        margin-left: 16rpx;
        font-size: 28rpx;
        color: #333;
      }
    }
    
    .expire-badge {
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      font-size: 22rpx;
      
      &.urgent {
        background: #ffebee;
        color: #f44336;
      }
      
      &.warning {
        background: #fff3e0;
        color: #ff9800;
      }
      
      &.normal {
        background: #e8f5e9;
        color: #4caf50;
      }
    }
  }
  
  .card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    
    .equity-info {
      display: flex;
      align-items: center;
      
      .type-tag {
        padding: 6rpx 12rpx;
        border-radius: 6rpx;
        font-size: 22rpx;
        
        &.voucher {
          background: #fff3e0;
          color: #f7931e;
        }
        
        &.deposit {
          background: #e8f5e9;
          color: #4caf50;
        }
        
        &.points {
          background: #e3f2fd;
          color: #2196f3;
        }
      }
      
      .equity-name {
        margin-left: 16rpx;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
    }
    
    .value-section {
      text-align: right;
      
      .value {
        display: block;
        font-size: 40rpx;
        font-weight: bold;
        color: #ff6b35;
      }
      
      .expire-date {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;
    background: #fafafa;
    
    .hint {
      font-size: 24rpx;
      color: #999;
    }
    
    .arrow {
      font-size: 32rpx;
      color: #ccc;
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
    font-size: 32rpx;
    color: #999;
    margin-top: 32rpx;
  }
  
  .empty-hint {
    font-size: 26rpx;
    color: #4caf50;
    margin-top: 16rpx;
  }
}

.loading-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
