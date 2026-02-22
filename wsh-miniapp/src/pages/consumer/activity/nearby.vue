<template>
  <view class="nearby-page">
    <!-- 地图区域 -->
    <view class="map-section">
      <map
        id="nearbyMap"
        class="nearby-map"
        :latitude="location.latitude"
        :longitude="location.longitude"
        :markers="markers"
        :scale="14"
        show-location
        @markertap="onMarkerTap"
      />
      <view class="map-overlay">
        <view class="location-bar" @click="refreshLocation">
          <text class="icon">📍</text>
          <text class="address">{{ currentAddress || '正在定位...' }}</text>
          <text class="refresh">刷新</text>
        </view>
      </view>
    </view>

    <!-- 商户/活动列表 -->
    <view class="list-section">
      <view class="section-header">
        <text class="title">{{ listTitle }}</text>
        <view class="tabs">
          <text :class="{ active: listType === 'activity' }" @click="listType = 'activity'">活动</text>
          <text :class="{ active: listType === 'merchant' }" @click="listType = 'merchant'">商户</text>
        </view>
      </view>

      <!-- 活动列表 -->
      <view v-if="listType === 'activity'" class="activity-list">
        <view v-for="item in activities" :key="item.id" class="activity-card" @click="goActivityDetail(item.id)">
          <image class="cover" :src="item.coverUrl" mode="aspectFill" />
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <view class="merchant-row">
              <image class="merchant-logo" :src="item.merchantLogo" mode="aspectFill" />
              <text class="merchant-name">{{ item.merchantName }}</text>
            </view>
            <view class="bottom-row">
              <view class="price-section">
                <text class="price">¥{{ item.price }}</text>
                <text v-if="item.originalPrice" class="original">¥{{ item.originalPrice }}</text>
              </view>
              <text class="distance">{{ item.distance }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 商户列表 -->
      <view v-else class="merchant-list">
        <view v-for="item in merchants" :key="item.id" class="merchant-card" @click="goMerchantDetail(item.id)">
          <image class="logo" :src="item.logoUrl" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <text class="address">{{ item.address }}</text>
            <view class="stats">
              <text class="activity-count">{{ item.activityCount }}个活动</text>
              <text class="distance">{{ item.distance }}</text>
            </view>
          </view>
          <view class="actions">
            <view class="action-btn" @click.stop="callMerchant(item.phone)">
              <text>📞</text>
            </view>
            <view class="action-btn" @click.stop="navigateToMerchant(item)">
              <text>🧭</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="(listType === 'activity' && activities.length === 0) || (listType === 'merchant' && merchants.length === 0)" class="empty-state">
        <text class="empty-text">附近暂无{{ listType === 'activity' ? '活动' : '商户' }}</text>
        <text class="empty-hint">换个位置试试吧</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { get } from '@/api/request'
import { useAppStore } from '@/store/app'

interface LocationInfo {
  latitude: number
  longitude: number
}

interface ActivityItem {
  id: string
  title: string
  coverUrl: string
  price: number
  originalPrice?: number
  merchantId: string
  merchantName: string
  merchantLogo: string
  distance: string
  latitude: number
  longitude: number
}

interface MerchantItem {
  id: string
  name: string
  logoUrl: string
  address: string
  phone: string
  activityCount: number
  distance: string
  latitude: number
  longitude: number
}

const appStore = useAppStore()

const loading = ref(false)
const listType = ref<'activity' | 'merchant'>('activity')
const location = ref<LocationInfo>({ latitude: 0, longitude: 0 })
const currentAddress = ref('')
const activities = ref<ActivityItem[]>([])
const merchants = ref<MerchantItem[]>([])
const targetMerchantId = ref('')

const listTitle = computed(() => {
  if (targetMerchantId.value) {
    return '商户门店'
  }
  return `附近${listType.value === 'activity' ? '活动' : '商户'}`
})

const markers = computed(() => {
  if (listType.value === 'activity') {
    return activities.value.map((item, index) => ({
      id: index,
      latitude: item.latitude,
      longitude: item.longitude,
      iconPath: '/static/marker-activity.png',
      width: 32,
      height: 40,
      callout: {
        content: item.title,
        display: 'BYCLICK',
        padding: 10,
        borderRadius: 6
      }
    }))
  } else {
    return merchants.value.map((item, index) => ({
      id: index,
      latitude: item.latitude,
      longitude: item.longitude,
      iconPath: '/static/marker-merchant.png',
      width: 32,
      height: 40,
      callout: {
        content: item.name,
        display: 'BYCLICK',
        padding: 10,
        borderRadius: 6
      }
    }))
  }
})

const getLocation = async () => {
  try {
    const res = await uni.getLocation({ type: 'gcj02' })
    location.value = {
      latitude: res.latitude,
      longitude: res.longitude
    }
    // 逆地理编码获取地址
    currentAddress.value = appStore.currentCity || '当前位置'
    return true
  } catch (e) {
    uni.showToast({ title: '定位失败，请检查权限', icon: 'none' })
    return false
  }
}

const refreshLocation = async () => {
  await getLocation()
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    if (listType.value === 'activity') {
      const res = await get<{ list: ActivityItem[] }>('/public/activities/nearby', {
        latitude: location.value.latitude,
        longitude: location.value.longitude,
        radius: 5000, // 5公里范围
        merchantId: targetMerchantId.value || undefined
      })
      if (res.code === 0) {
        activities.value = res.data.list || []
      }
    } else {
      const res = await get<{ list: MerchantItem[] }>('/public/merchants/nearby', {
        latitude: location.value.latitude,
        longitude: location.value.longitude,
        radius: 5000
      })
      if (res.code === 0) {
        merchants.value = res.data.list || []
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const onMarkerTap = (e: any) => {
  const index = e.markerId
  if (listType.value === 'activity' && activities.value[index]) {
    goActivityDetail(activities.value[index].id)
  } else if (listType.value === 'merchant' && merchants.value[index]) {
    goMerchantDetail(merchants.value[index].id)
  }
}

const goActivityDetail = (id: string) => {
  uni.navigateTo({ url: `/pages/consumer/activity/detail?id=${id}` })
}

const goMerchantDetail = (id: string) => {
  uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${id}` })
}

const callMerchant = (phone: string) => {
  if (phone) {
    uni.makePhoneCall({ phoneNumber: phone })
  } else {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
  }
}

const navigateToMerchant = (item: MerchantItem) => {
  uni.openLocation({
    latitude: item.latitude,
    longitude: item.longitude,
    name: item.name,
    address: item.address
  })
}

watch(listType, () => {
  loadData()
})

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  targetMerchantId.value = currentPage.options?.merchantId || ''
  
  const success = await getLocation()
  if (success) {
    loadData()
  }
})
</script>

<style lang="scss">
.nearby-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.map-section {
  position: relative;
  height: 400rpx;
  
  .nearby-map {
    width: 100%;
    height: 100%;
  }
  
  .map-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    padding: 24rpx;
    
    .location-bar {
      display: flex;
      align-items: center;
      background: #fff;
      padding: 16rpx 24rpx;
      border-radius: 32rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
      
      .icon {
        font-size: 28rpx;
      }
      
      .address {
        flex: 1;
        margin-left: 12rpx;
        font-size: 28rpx;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .refresh {
        font-size: 26rpx;
        color: #ff6b35;
      }
    }
  }
}

.list-section {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  margin-top: -24rpx;
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 376rpx);
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .tabs {
    display: flex;
    
    text {
      margin-left: 24rpx;
      font-size: 28rpx;
      color: #999;
      padding: 8rpx 20rpx;
      
      &.active {
        color: #ff6b35;
        background: #fff3e0;
        border-radius: 20rpx;
      }
    }
  }
}

.activity-list {
  .activity-card {
    display: flex;
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
    
    .cover {
      width: 200rpx;
      height: 200rpx;
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      padding: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .merchant-row {
        display: flex;
        align-items: center;
        margin-top: 12rpx;
        
        .merchant-logo {
          width: 36rpx;
          height: 36rpx;
          border-radius: 6rpx;
        }
        
        .merchant-name {
          margin-left: 8rpx;
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .bottom-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12rpx;
        
        .price-section {
          .price {
            font-size: 32rpx;
            font-weight: bold;
            color: #ff6b35;
          }
          
          .original {
            font-size: 22rpx;
            color: #999;
            text-decoration: line-through;
            margin-left: 8rpx;
          }
        }
        
        .distance {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.merchant-list {
  .merchant-card {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 16rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
    
    .logo {
      width: 120rpx;
      height: 120rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      margin-left: 20rpx;
      
      .name {
        display: block;
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .address {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .stats {
        display: flex;
        justify-content: space-between;
        margin-top: 12rpx;
        
        .activity-count {
          font-size: 24rpx;
          color: #ff6b35;
        }
        
        .distance {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
    
    .actions {
      display: flex;
      flex-direction: column;
      
      .action-btn {
        width: 60rpx;
        height: 60rpx;
        background: #f5f5f5;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12rpx;
        font-size: 28rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60rpx 40rpx;
  
  .empty-text {
    display: block;
    font-size: 28rpx;
    color: #999;
  }
  
  .empty-hint {
    display: block;
    font-size: 24rpx;
    color: #bbb;
    margin-top: 12rpx;
  }
}

.loading-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
