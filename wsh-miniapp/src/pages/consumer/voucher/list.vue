<template>
  <view class="voucher-list-page">
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
      </view>
    </view>

    <!-- 券列表 -->
    <view class="voucher-list">
      <view 
        v-for="item in filteredVouchers" 
        :key="item.id" 
        class="voucher-card"
        :class="{ expired: item.status === 'expired', used: item.status === 'used' }"
        @click="goDetail(item.id)"
      >
        <!-- 左侧金额/折扣 -->
        <view class="voucher-left">
          <view class="amount-section">
            <text v-if="item.type === 'cash'" class="currency">¥</text>
            <text class="amount">{{ item.value }}</text>
            <text v-if="item.type === 'discount'" class="unit">折</text>
          </view>
          <text class="condition">{{ item.condition }}</text>
        </view>
        
        <!-- 中间信息 -->
        <view class="voucher-center">
          <text class="name">{{ item.name }}</text>
          <view class="merchant-row">
            <image class="logo" :src="item.merchantLogo" mode="aspectFill" />
            <text class="merchant-name">{{ item.merchantName }}</text>
          </view>
          <text class="expire">{{ item.expireDate }}到期</text>
        </view>
        
        <!-- 右侧操作 -->
        <view class="voucher-right">
          <view v-if="item.status === 'unused'" class="use-btn" @click.stop="handleUse(item)">
            <text>去使用</text>
          </view>
          <view v-else class="status-tag" :class="item.status">
            <text>{{ getStatusText(item.status) }}</text>
          </view>
        </view>
        
        <!-- 装饰圆孔 -->
        <view class="hole hole-top"></view>
        <view class="hole hole-bottom"></view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && filteredVouchers.length === 0" class="empty-state">
        <image src="/static/empty-voucher.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无优惠券</text>
        <button class="go-btn" @click="goActivity">去领券</button>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getVouchers } from '@/api/voucher'

interface VoucherItem {
  id: string
  name: string
  type: 'cash' | 'discount' | 'gift'
  value: number
  condition: string
  merchantId: string
  merchantName: string
  merchantLogo: string
  expireDate: string
  status: 'unused' | 'used' | 'expired'
}

const statusTabs = [
  { key: 'all', label: '全部' },
  { key: 'unused', label: '可使用' },
  { key: 'used', label: '已使用' },
  { key: 'expired', label: '已过期' }
]

const loading = ref(true)
const currentStatus = ref('all')
const vouchers = ref<VoucherItem[]>([])
const voucherType = ref('')

const filteredVouchers = computed(() => {
  if (currentStatus.value === 'all') {
    return vouchers.value
  }
  return vouchers.value.filter(v => v.status === currentStatus.value)
})

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    used: '已使用',
    expired: '已过期'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVouchers({
      type: voucherType.value || undefined,
      status: currentStatus.value === 'all' ? undefined : currentStatus.value
    })
    if (res.code === 0) {
      vouchers.value = res.data.list || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goDetail = (id: string) => {
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${id}` })
}

const handleUse = (item: VoucherItem) => {
  // 跳转到商户活动页或直接展示券码
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${item.id}` })
}

const goActivity = () => {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  voucherType.value = currentPage.options?.type || ''
  loadData()
})
</script>

<style lang="scss">
.voucher-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 0 24rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .filter-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    color: #666;
    position: relative;
    
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
  }
}

.voucher-list {
  padding: 24rpx;
}

.voucher-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  position: relative;
  
  &.expired, &.used {
    opacity: 0.6;
    
    .voucher-left {
      background: #999;
    }
  }
  
  .voucher-left {
    width: 200rpx;
    padding: 24rpx;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .amount-section {
      display: flex;
      align-items: baseline;
      
      .currency {
        font-size: 28rpx;
        color: #fff;
      }
      
      .amount {
        font-size: 56rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .unit {
        font-size: 24rpx;
        color: #fff;
        margin-left: 4rpx;
      }
    }
    
    .condition {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.9);
      margin-top: 8rpx;
    }
  }
  
  .voucher-center {
    flex: 1;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .name {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }
    
    .merchant-row {
      display: flex;
      align-items: center;
      margin-top: 12rpx;
      
      .logo {
        width: 32rpx;
        height: 32rpx;
        border-radius: 6rpx;
      }
      
      .merchant-name {
        margin-left: 8rpx;
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .expire {
      font-size: 22rpx;
      color: #999;
      margin-top: 12rpx;
    }
  }
  
  .voucher-right {
    display: flex;
    align-items: center;
    padding-right: 24rpx;
    
    .use-btn {
      padding: 16rpx 24rpx;
      background: linear-gradient(135deg, #ff6b35, #f7931e);
      border-radius: 32rpx;
      
      text {
        font-size: 24rpx;
        color: #fff;
      }
    }
    
    .status-tag {
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
      
      &.used {
        background: #f5f5f5;
        
        text {
          font-size: 22rpx;
          color: #999;
        }
      }
      
      &.expired {
        background: #f5f5f5;
        
        text {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
  
  // 装饰圆孔
  .hole {
    position: absolute;
    left: 192rpx;
    width: 24rpx;
    height: 24rpx;
    background: #f5f5f5;
    border-radius: 50%;
    
    &.hole-top {
      top: -12rpx;
    }
    
    &.hole-bottom {
      bottom: -12rpx;
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

.loading-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
