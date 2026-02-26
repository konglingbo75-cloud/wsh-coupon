<template>
  <view class="activity-page">
    <!-- 城市选择器 -->
    <CitySelector 
      v-model:visible="showCitySelector" 
      @select="onCitySelected"
    />
    
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="city-btn" @tap="openCitySelector">
        <text>{{ appStore.currentCityName }}</text>
        <text class="arrow">▼</text>
      </view>
      <view class="search-input" @tap="goSearch">
        <text class="icon">🔍</text>
        <text class="placeholder">搜索活动或商户</text>
      </view>
    </view>
    
    <!-- 活动类型Tab -->
    <view class="type-tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentType === 'all' }"
        @tap="switchType('all')"
      >
        <text>全部</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentType === 'voucher' }"
        @tap="switchType('voucher')"
      >
        <text>代金券</text>
        <text class="count" v-if="typeCount.voucher">({{ typeCount.voucher }})</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentType === 'deposit' }"
        @tap="switchType('deposit')"
      >
        <text>储值</text>
        <text class="count" v-if="typeCount.deposit">({{ typeCount.deposit }})</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentType === 'points' }"
        @tap="switchType('points')"
      >
        <text>积分兑换</text>
        <text class="count" v-if="typeCount.points">({{ typeCount.points }})</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentType === 'group' }"
        @tap="switchType('group')"
      >
        <text>团购</text>
        <text class="count" v-if="typeCount.group">({{ typeCount.group }})</text>
      </view>
    </view>
    
    <!-- 活动列表 -->
    <scroll-view 
      class="activity-list" 
      scroll-y 
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view 
        class="activity-item" 
        v-for="item in filteredActivities" 
        :key="item.activityId"
        @tap="goDetail(item.activityId)"
      >
        <image class="cover" :src="item.coverImage || '/static/default-activity.png'" mode="aspectFill" />
        <view class="content">
          <view class="merchant-row">
            <image class="merchant-logo" :src="item.merchantLogo || '/static/default-merchant.png'" />
            <text class="merchant-name">{{ item.merchantName }}</text>
          </view>
          <text class="activity-name">{{ item.activityName }}</text>
          <text class="activity-desc">{{ item.activityDesc }}</text>
          <view class="bottom-row">
            <view class="price-info">
              <text class="price">¥{{ item.sellingPrice || 0 }}</text>
              <text class="original" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
            </view>
            <view class="sold-info">
              <text>已售 {{ item.soldCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-if="filteredActivities.length === 0 && !loading">
        <text class="icon">📭</text>
        <text>暂无活动</text>
      </view>
      
      <view class="loading-tip" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/store/app'
import { getPublicActivities, type PublicActivity, type PublicActivitiesResponse } from '@/api/public'
import CitySelector from '@/components/CitySelector.vue'
import type { CityItem } from '@/api/city'

const appStore = useAppStore()

const currentType = ref<'all' | 'voucher' | 'deposit' | 'points' | 'group'>('all')
const loading = ref(false)
const refreshing = ref(false)
const showCitySelector = ref(false)

const activitiesData = ref<PublicActivitiesResponse | null>(null)

const typeCount = computed(() => {
  return activitiesData.value?.typeCount || {
    voucher: 0,
    deposit: 0,
    points: 0,
    group: 0,
    total: 0
  }
})

const filteredActivities = computed(() => {
  if (!activitiesData.value) return []
  
  switch (currentType.value) {
    case 'voucher':
      return activitiesData.value.voucherActivities
    case 'deposit':
      return activitiesData.value.depositActivities
    case 'points':
      return activitiesData.value.pointsActivities
    case 'group':
      return activitiesData.value.groupActivities
    default:
      return [
        ...activitiesData.value.voucherActivities,
        ...activitiesData.value.depositActivities,
        ...activitiesData.value.pointsActivities,
        ...activitiesData.value.groupActivities
      ]
  }
})

onShow(() => {
  loadActivities()
})

async function loadActivities() {
  loading.value = true
  try {
    activitiesData.value = await getPublicActivities(appStore.currentCityName)
  } catch (err) {
    console.error('加载活动失败', err)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  refreshing.value = true
  loadActivities()
}

function switchType(type: typeof currentType.value) {
  currentType.value = type
}

function openCitySelector() {
  showCitySelector.value = true
}

function onCitySelected(city: CityItem) {
  // 城市切换后重新加载数据
  loadActivities()
}

function goSearch() {
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/subPackages/consumer/activity/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.activity-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  
  .city-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 16rpx 20rpx;
    background: #f5f7fa;
    border-radius: 8rpx;
    font-size: 28rpx;
    
    .arrow {
      font-size: 20rpx;
      color: #999;
    }
  }
  
  .search-input {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 24rpx;
    background: #f5f7fa;
    border-radius: 32rpx;
    
    .icon {
      font-size: 28rpx;
    }
    
    .placeholder {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.type-tabs {
  display: flex;
  padding: 0 24rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  overflow-x: auto;
  white-space: nowrap;
  
  .tab-item {
    display: flex;
    align-items: center;
    padding: 24rpx 20rpx;
    font-size: 28rpx;
    color: #666;
    position: relative;
    
    &.active {
      color: #667eea;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }
    
    .count {
      font-size: 24rpx;
      margin-left: 4rpx;
    }
  }
}

.activity-list {
  flex: 1;
  padding: 24rpx;
  
  .activity-item {
    display: flex;
    gap: 20rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    background: #fff;
    border-radius: 16rpx;
    
    .cover {
      width: 200rpx;
      height: 200rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }
    
    .content {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .merchant-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 12rpx;
        
        .merchant-logo {
          width: 40rpx;
          height: 40rpx;
          border-radius: 8rpx;
        }
        
        .merchant-name {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .activity-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 8rpx;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .activity-desc {
        font-size: 24rpx;
        color: #999;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        flex: 1;
      }
      
      .bottom-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12rpx;
        
        .price-info {
          display: flex;
          align-items: baseline;
          gap: 12rpx;
          
          .price {
            font-size: 36rpx;
            font-weight: 600;
            color: #e53935;
          }
          
          .original {
            font-size: 24rpx;
            color: #999;
            text-decoration: line-through;
          }
        }
        
        .sold-info {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
  
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 100rpx 0;
    
    .icon {
      font-size: 80rpx;
      margin-bottom: 20rpx;
    }
    
    text {
      color: #999;
      font-size: 28rpx;
    }
  }
  
  .loading-tip {
    text-align: center;
    padding: 40rpx;
    color: #999;
    font-size: 28rpx;
  }
}
</style>
