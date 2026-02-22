<template>
  <view class="equity-summary-page">
    <!-- 总览卡片 -->
    <view class="summary-card">
      <view class="total-section">
        <text class="label">权益总价值</text>
        <view class="amount-row">
          <text class="currency">¥</text>
          <text class="amount">{{ formatMoney(summary?.totalValue || 0) }}</text>
        </view>
      </view>
      <view class="detail-grid">
        <view class="detail-item" @click="goVouchers">
          <text class="value">{{ summary?.voucherValue || 0 }}</text>
          <text class="label">优惠券(元)</text>
        </view>
        <view class="detail-item" @click="goDeposits">
          <text class="value">{{ summary?.depositValue || 0 }}</text>
          <text class="label">储值余额(元)</text>
        </view>
        <view class="detail-item" @click="goPoints">
          <text class="value">{{ summary?.pointsValue || 0 }}</text>
          <text class="label">积分价值(元)</text>
        </view>
      </view>
    </view>

    <!-- 即将过期提醒 -->
    <view v-if="expiringList.length > 0" class="expiring-section">
      <view class="section-header">
        <text class="title">即将过期</text>
        <view class="more" @click="goExpiring">
          <text>查看全部</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="expiring-list">
        <view v-for="item in expiringList.slice(0, 3)" :key="item.id" class="expiring-item" @click="goEquityDetail(item)">
          <view class="info">
            <text class="name">{{ item.equityName }}</text>
            <text class="merchant">{{ item.merchantName }}</text>
          </view>
          <view class="right">
            <text class="value">¥{{ item.value }}</text>
            <text class="expire">{{ item.daysLeft }}天后过期</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 权益分类列表 -->
    <view class="equity-categories">
      <view class="section-header">
        <text class="title">我的权益</text>
      </view>
      
      <!-- 按商户分组 -->
      <view v-for="merchant in merchantEquities" :key="merchant.merchantId" class="merchant-group">
        <view class="merchant-header" @click="goMerchantDetail(merchant.merchantId)">
          <image class="logo" :src="merchant.logoUrl || '/static/default-merchant.png'" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ merchant.merchantName }}</text>
            <text class="count">{{ merchant.equityCount }}项权益</text>
          </view>
          <text class="total-value">¥{{ formatMoney(merchant.totalValue) }}</text>
        </view>
        <view class="equity-list">
          <view v-for="equity in merchant.equities.slice(0, 2)" :key="equity.id" class="equity-item" @click="goEquityDetail(equity)">
            <view class="type-tag" :class="equity.type">{{ getTypeName(equity.type) }}</view>
            <view class="content">
              <text class="name">{{ equity.name }}</text>
              <text class="desc">{{ equity.description }}</text>
            </view>
            <text class="value">¥{{ equity.value }}</text>
          </view>
          <view v-if="merchant.equities.length > 2" class="show-more" @click="toggleMerchantExpand(merchant.merchantId)">
            <text>查看更多({{ merchant.equities.length - 2 }}项)</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && merchantEquities.length === 0" class="empty-state">
      <image src="/static/empty-equity.png" mode="aspectFit" class="empty-icon" />
      <text class="empty-text">暂无权益</text>
      <text class="empty-hint">去参与活动获取更多权益吧</text>
      <button class="go-activity-btn" @click="goActivityList">去逛逛</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useEquityStore } from '@/store/equity'
import { getEquitySummary, getExpiringEquities } from '@/api/equity'

interface EquityItem {
  id: string
  name: string
  type: 'voucher' | 'deposit' | 'points'
  value: number
  description: string
  expireDate: string
  merchantId: string
  merchantName: string
}

interface MerchantEquity {
  merchantId: string
  merchantName: string
  logoUrl: string
  totalValue: number
  equityCount: number
  equities: EquityItem[]
}

interface ExpiringItem {
  id: string
  equityName: string
  merchantName: string
  value: number
  daysLeft: number
}

const equityStore = useEquityStore()

const loading = ref(true)
const summary = ref<{
  totalValue: number
  voucherValue: number
  depositValue: number
  pointsValue: number
} | null>(null)
const expiringList = ref<ExpiringItem[]>([])
const merchantEquities = ref<MerchantEquity[]>([])
const expandedMerchants = ref<Set<string>>(new Set())

const formatMoney = (value: number) => {
  return value.toFixed(2)
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    voucher: '优惠券',
    deposit: '储值',
    points: '积分'
  }
  return map[type] || type
}

const loadData = async () => {
  loading.value = true
  try {
    // 并行加载数据
    const [summaryRes, expiringRes] = await Promise.all([
      getEquitySummary(),
      getExpiringEquities({ days: 7, limit: 10 })
    ])
    
    if (summaryRes.code === 0) {
      summary.value = summaryRes.data
      // 模拟按商户分组的数据（实际应从API获取）
      merchantEquities.value = summaryRes.data.merchantEquities || []
    }
    
    if (expiringRes.code === 0) {
      expiringList.value = expiringRes.data.list || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goVouchers = () => {
  uni.navigateTo({ url: '/pages/consumer/voucher/list?type=voucher' })
}

const goDeposits = () => {
  uni.navigateTo({ url: '/pages/consumer/voucher/list?type=deposit' })
}

const goPoints = () => {
  uni.navigateTo({ url: '/pages/consumer/voucher/list?type=points' })
}

const goExpiring = () => {
  uni.navigateTo({ url: '/pages/consumer/equity/expiring' })
}

const goEquityDetail = (item: any) => {
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${item.id}` })
}

const goMerchantDetail = (merchantId: string) => {
  uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${merchantId}` })
}

const toggleMerchantExpand = (merchantId: string) => {
  if (expandedMerchants.value.has(merchantId)) {
    expandedMerchants.value.delete(merchantId)
  } else {
    expandedMerchants.value.add(merchantId)
  }
}

const goActivityList = () => {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss">
.equity-summary-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.summary-card {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  margin: 24rpx;
  border-radius: 24rpx;
  padding: 40rpx;
  color: #fff;
  
  .total-section {
    text-align: center;
    padding-bottom: 32rpx;
    border-bottom: 1rpx solid rgba(255,255,255,0.2);
    
    .label {
      font-size: 28rpx;
      opacity: 0.9;
    }
    
    .amount-row {
      display: flex;
      align-items: baseline;
      justify-content: center;
      margin-top: 16rpx;
      
      .currency {
        font-size: 36rpx;
        margin-right: 8rpx;
      }
      
      .amount {
        font-size: 72rpx;
        font-weight: bold;
      }
    }
  }
  
  .detail-grid {
    display: flex;
    justify-content: space-around;
    padding-top: 32rpx;
    
    .detail-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 40rpx;
        font-weight: bold;
      }
      
      .label {
        display: block;
        font-size: 24rpx;
        opacity: 0.8;
        margin-top: 8rpx;
      }
    }
  }
}

.expiring-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
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
  
  .more {
    display: flex;
    align-items: center;
    color: #999;
    font-size: 26rpx;
    
    .arrow {
      margin-left: 8rpx;
    }
  }
}

.expiring-list {
  .expiring-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info {
      .name {
        display: block;
        font-size: 28rpx;
        color: #333;
      }
      
      .merchant {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
    
    .right {
      text-align: right;
      
      .value {
        display: block;
        font-size: 32rpx;
        color: #ff6b35;
        font-weight: bold;
      }
      
      .expire {
        display: block;
        font-size: 22rpx;
        color: #ff4757;
        margin-top: 8rpx;
      }
    }
  }
}

.equity-categories {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.merchant-group {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .merchant-header {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: #fafafa;
    border-radius: 12rpx;
    
    .logo {
      width: 80rpx;
      height: 80rpx;
      border-radius: 12rpx;
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
      
      .count {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
    
    .total-value {
      font-size: 36rpx;
      font-weight: bold;
      color: #ff6b35;
    }
  }
  
  .equity-list {
    padding: 16rpx 0;
    
    .equity-item {
      display: flex;
      align-items: center;
      padding: 20rpx;
      border-bottom: 1rpx solid #f5f5f5;
      
      .type-tag {
        padding: 8rpx 16rpx;
        border-radius: 8rpx;
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
      
      .content {
        flex: 1;
        margin-left: 20rpx;
        
        .name {
          display: block;
          font-size: 28rpx;
          color: #333;
        }
        
        .desc {
          display: block;
          font-size: 24rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }
      
      .value {
        font-size: 32rpx;
        font-weight: bold;
        color: #ff6b35;
      }
    }
    
    .show-more {
      text-align: center;
      padding: 20rpx;
      color: #666;
      font-size: 26rpx;
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
    color: #bbb;
    margin-top: 16rpx;
  }
  
  .go-activity-btn {
    margin-top: 40rpx;
    padding: 20rpx 60rpx;
    background: #ff6b35;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
    border: none;
  }
}
</style>
