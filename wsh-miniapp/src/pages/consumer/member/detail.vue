<template>
  <view class="member-detail-page">
    <!-- 商户头部 -->
    <view class="merchant-header">
      <image class="cover" :src="merchant?.coverUrl || '/static/default-cover.png'" mode="aspectFill" />
      <view class="overlay"></view>
      <view class="merchant-info">
        <image class="logo" :src="merchant?.logoUrl || '/static/default-merchant.png'" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ merchant?.name }}</text>
          <view v-if="memberInfo?.vipLevel" class="vip-badge">VIP{{ memberInfo.vipLevel }}</view>
        </view>
      </view>
    </view>

    <!-- 会员卡片 -->
    <view class="member-card">
      <view class="card-header">
        <text class="label">会员编号</text>
        <text class="member-no">{{ memberInfo?.memberNo }}</text>
      </view>
      <view class="card-body">
        <view class="qrcode-section" @click="showQrcode">
          <image class="qrcode" :src="memberInfo?.qrcodeUrl || '/static/default-qrcode.png'" mode="aspectFit" />
          <text class="hint">点击放大</text>
        </view>
        <view class="equity-section">
          <view class="equity-item" @click="goVouchers">
            <text class="num">{{ memberInfo?.voucherCount || 0 }}</text>
            <text class="label">优惠券</text>
          </view>
          <view class="equity-item" @click="goDeposit">
            <text class="num">¥{{ memberInfo?.depositBalance || 0 }}</text>
            <text class="label">储值余额</text>
          </view>
          <view class="equity-item" @click="goPoints">
            <text class="num">{{ memberInfo?.points || 0 }}</text>
            <text class="label">积分</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="function-grid">
      <view class="function-item" @click="goOrders">
        <view class="icon-wrap order">📋</view>
        <text class="label">订单记录</text>
      </view>
      <view class="function-item" @click="goActivities">
        <view class="icon-wrap activity">🎉</view>
        <text class="label">商户活动</text>
      </view>
      <view class="function-item" @click="goNearby">
        <view class="icon-wrap location">📍</view>
        <text class="label">门店导航</text>
      </view>
      <view class="function-item" @click="callMerchant">
        <view class="icon-wrap phone">📞</view>
        <text class="label">联系商户</text>
      </view>
    </view>

    <!-- 权益明细 -->
    <view class="equity-detail-section">
      <view class="section-header">
        <text class="title">权益明细</text>
        <view class="tabs">
          <text 
            v-for="tab in equityTabs" 
            :key="tab.key" 
            :class="{ active: currentTab === tab.key }"
            @click="currentTab = tab.key"
          >{{ tab.label }}</text>
        </view>
      </view>
      
      <view class="equity-list">
        <view v-for="item in currentEquities" :key="item.id" class="equity-item" @click="goEquityDetail(item)">
          <view class="left">
            <view class="type-tag" :class="item.type">{{ getTypeName(item.type) }}</view>
            <view class="info">
              <text class="name">{{ item.name }}</text>
              <text class="expire">{{ item.expireDate }}到期</text>
            </view>
          </view>
          <view class="right">
            <text class="value">{{ item.type === 'points' ? item.value : '¥' + item.value }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
        
        <view v-if="currentEquities.length === 0" class="empty-equity">
          <text>暂无{{ equityTabs.find(t => t.key === currentTab)?.label }}</text>
        </view>
      </view>
    </view>

    <!-- 交易记录 -->
    <view class="transaction-section">
      <view class="section-header">
        <text class="title">最近交易</text>
        <view class="more" @click="goTransactions">
          <text>全部记录</text>
          <text class="arrow">›</text>
        </view>
      </view>
      
      <view class="transaction-list">
        <view v-for="item in recentTransactions" :key="item.id" class="transaction-item">
          <view class="info">
            <text class="desc">{{ item.description }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
          <text class="amount" :class="{ income: item.amount > 0 }">
            {{ item.amount > 0 ? '+' : '' }}{{ item.amount }}{{ item.unit }}
          </text>
        </view>
        
        <view v-if="recentTransactions.length === 0" class="empty-transaction">
          <text>暂无交易记录</text>
        </view>
      </view>
    </view>

    <!-- 二维码弹窗 -->
    <view v-if="showQrcodeModal" class="qrcode-modal" @click="showQrcodeModal = false">
      <view class="modal-content" @click.stop>
        <image class="big-qrcode" :src="memberInfo?.qrcodeUrl || '/static/default-qrcode.png'" mode="aspectFit" />
        <text class="member-no">会员编号: {{ memberInfo?.memberNo }}</text>
        <text class="hint">出示此码进行会员识别</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get } from '@/api/request'

interface MerchantInfo {
  id: string
  name: string
  logoUrl: string
  coverUrl: string
  phone: string
  address: string
}

interface MemberInfo {
  memberNo: string
  vipLevel: number
  qrcodeUrl: string
  voucherCount: number
  depositBalance: number
  points: number
  joinDate: string
}

interface EquityItem {
  id: string
  name: string
  type: 'voucher' | 'deposit' | 'points'
  value: number
  expireDate: string
}

interface TransactionItem {
  id: string
  description: string
  amount: number
  unit: string
  time: string
}

const merchantId = ref('')
const merchant = ref<MerchantInfo | null>(null)
const memberInfo = ref<MemberInfo | null>(null)
const vouchers = ref<EquityItem[]>([])
const deposits = ref<EquityItem[]>([])
const points = ref<EquityItem[]>([])
const recentTransactions = ref<TransactionItem[]>([])
const showQrcodeModal = ref(false)
const currentTab = ref('voucher')

const equityTabs = [
  { key: 'voucher', label: '优惠券' },
  { key: 'deposit', label: '储值' },
  { key: 'points', label: '积分' }
]

const currentEquities = computed(() => {
  switch (currentTab.value) {
    case 'voucher': return vouchers.value
    case 'deposit': return deposits.value
    case 'points': return points.value
    default: return []
  }
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    voucher: '券',
    deposit: '储',
    points: '分'
  }
  return map[type] || type
}

const loadData = async () => {
  try {
    const res = await get<{
      merchant: MerchantInfo
      memberInfo: MemberInfo
      vouchers: EquityItem[]
      deposits: EquityItem[]
      points: EquityItem[]
      transactions: TransactionItem[]
    }>(`/consumer/members/${merchantId.value}`)
    
    if (res.code === 0) {
      merchant.value = res.data.merchant
      memberInfo.value = res.data.memberInfo
      vouchers.value = res.data.vouchers || []
      deposits.value = res.data.deposits || []
      points.value = res.data.points || []
      recentTransactions.value = res.data.transactions || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const showQrcode = () => {
  showQrcodeModal.value = true
}

const goVouchers = () => {
  currentTab.value = 'voucher'
}

const goDeposit = () => {
  currentTab.value = 'deposit'
}

const goPoints = () => {
  currentTab.value = 'points'
}

const goOrders = () => {
  uni.navigateTo({ url: `/pages/consumer/order/list?merchantId=${merchantId.value}` })
}

const goActivities = () => {
  uni.navigateTo({ url: `/pages/consumer/activity/nearby?merchantId=${merchantId.value}` })
}

const goNearby = () => {
  if (merchant.value?.address) {
    uni.openLocation({
      latitude: 0, // 实际应从商户数据获取
      longitude: 0,
      name: merchant.value.name,
      address: merchant.value.address
    })
  }
}

const callMerchant = () => {
  if (merchant.value?.phone) {
    uni.makePhoneCall({ phoneNumber: merchant.value.phone })
  } else {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
  }
}

const goEquityDetail = (item: EquityItem) => {
  uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${item.id}` })
}

const goTransactions = () => {
  uni.navigateTo({ url: `/pages/consumer/order/list?merchantId=${merchantId.value}&type=transaction` })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  merchantId.value = currentPage.options?.merchantId || ''
  if (merchantId.value) {
    loadData()
  }
})
</script>

<style lang="scss">
.member-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.merchant-header {
  position: relative;
  height: 320rpx;
  
  .cover {
    width: 100%;
    height: 100%;
  }
  
  .overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.5));
  }
  
  .merchant-info {
    position: absolute;
    bottom: 24rpx;
    left: 24rpx;
    display: flex;
    align-items: center;
    
    .logo {
      width: 100rpx;
      height: 100rpx;
      border-radius: 16rpx;
      border: 4rpx solid #fff;
    }
    
    .info {
      margin-left: 20rpx;
      display: flex;
      align-items: center;
      
      .name {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .vip-badge {
        margin-left: 16rpx;
        padding: 6rpx 16rpx;
        background: linear-gradient(135deg, #ffd700, #ff9500);
        border-radius: 16rpx;
        font-size: 22rpx;
        color: #fff;
      }
    }
  }
}

.member-card {
  margin: -40rpx 24rpx 24rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  border-radius: 20rpx;
  position: relative;
  z-index: 1;
  
  .card-header {
    padding: 24rpx;
    border-bottom: 1rpx solid rgba(255,255,255,0.2);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .label {
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
    }
    
    .member-no {
      font-size: 28rpx;
      color: #fff;
      font-weight: bold;
    }
  }
  
  .card-body {
    display: flex;
    padding: 24rpx;
    
    .qrcode-section {
      text-align: center;
      padding: 16rpx;
      background: #fff;
      border-radius: 12rpx;
      
      .qrcode {
        width: 120rpx;
        height: 120rpx;
      }
      
      .hint {
        display: block;
        font-size: 20rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
    
    .equity-section {
      flex: 1;
      display: flex;
      justify-content: space-around;
      align-items: center;
      margin-left: 24rpx;
      
      .equity-item {
        text-align: center;
        
        .num {
          display: block;
          font-size: 36rpx;
          font-weight: bold;
          color: #fff;
        }
        
        .label {
          display: block;
          font-size: 22rpx;
          color: rgba(255,255,255,0.8);
          margin-top: 8rpx;
        }
      }
    }
  }
}

.function-grid {
  display: flex;
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx 0;
  
  .function-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .icon-wrap {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      
      &.order { background: #e3f2fd; }
      &.activity { background: #fff3e0; }
      &.location { background: #e8f5e9; }
      &.phone { background: #fce4ec; }
    }
    
    .label {
      margin-top: 12rpx;
      font-size: 24rpx;
      color: #666;
    }
  }
}

.equity-detail-section, .transaction-section {
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
  
  .tabs {
    display: flex;
    
    text {
      margin-left: 24rpx;
      font-size: 26rpx;
      color: #999;
      
      &.active {
        color: #ff6b35;
        font-weight: bold;
      }
    }
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

.equity-list {
  .equity-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .left {
      display: flex;
      align-items: center;
      
      .type-tag {
        width: 48rpx;
        height: 48rpx;
        border-radius: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        
        &.voucher { background: #fff3e0; color: #f7931e; }
        &.deposit { background: #e8f5e9; color: #4caf50; }
        &.points { background: #e3f2fd; color: #2196f3; }
      }
      
      .info {
        margin-left: 16rpx;
        
        .name {
          display: block;
          font-size: 28rpx;
          color: #333;
        }
        
        .expire {
          display: block;
          font-size: 22rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }
    }
    
    .right {
      display: flex;
      align-items: center;
      
      .value {
        font-size: 32rpx;
        font-weight: bold;
        color: #ff6b35;
      }
      
      .arrow {
        margin-left: 12rpx;
        font-size: 28rpx;
        color: #ccc;
      }
    }
  }
  
  .empty-equity {
    text-align: center;
    padding: 40rpx;
    color: #999;
    font-size: 28rpx;
  }
}

.transaction-list {
  .transaction-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info {
      .desc {
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
    
    .amount {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      
      &.income {
        color: #4caf50;
      }
    }
  }
  
  .empty-transaction {
    text-align: center;
    padding: 40rpx;
    color: #999;
    font-size: 28rpx;
  }
}

.qrcode-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .modal-content {
    background: #fff;
    border-radius: 24rpx;
    padding: 48rpx;
    text-align: center;
    
    .big-qrcode {
      width: 400rpx;
      height: 400rpx;
    }
    
    .member-no {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-top: 24rpx;
    }
    
    .hint {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 16rpx;
    }
  }
}
</style>
