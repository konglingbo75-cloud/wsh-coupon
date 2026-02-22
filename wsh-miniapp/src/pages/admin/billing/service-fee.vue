<template>
  <view class="service-fee-page">
    <!-- 账单概览 -->
    <view class="overview-card">
      <view class="overview-header">
        <text class="title">服务费账单</text>
        <picker mode="date" :value="selectedMonth" @change="onMonthChange" fields="month">
          <view class="month-picker">
            <text>{{ selectedMonth }}</text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>
      <view class="overview-body">
        <view class="main-amount">
          <text class="label">本月应付</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="amount">{{ overview.totalFee }}</text>
          </view>
        </view>
        <view class="detail-row">
          <view class="detail-item">
            <text class="num">¥{{ overview.orderFee }}</text>
            <text class="label">订单服务费</text>
          </view>
          <view class="detail-item">
            <text class="num">¥{{ overview.verifyFee }}</text>
            <text class="label">核销服务费</text>
          </view>
          <view class="detail-item">
            <text class="num">¥{{ overview.otherFee }}</text>
            <text class="label">其他费用</text>
          </view>
        </view>
      </view>
      <view class="pay-status" :class="overview.status">
        <text>{{ getStatusText(overview.status) }}</text>
      </view>
    </view>

    <!-- 费率说明 -->
    <view class="rate-section">
      <view class="section-header">
        <text class="title">费率说明</text>
      </view>
      <view class="rate-list">
        <view class="rate-item">
          <text class="label">订单服务费率</text>
          <text class="value">{{ rateInfo.orderRate }}%</text>
        </view>
        <view class="rate-item">
          <text class="label">核销服务费率</text>
          <text class="value">{{ rateInfo.verifyRate }}%</text>
        </view>
        <view class="rate-item">
          <text class="label">结算周期</text>
          <text class="value">{{ rateInfo.settlementCycle }}</text>
        </view>
      </view>
    </view>

    <!-- 历史账单 -->
    <view class="history-section">
      <view class="section-header">
        <text class="title">历史账单</text>
      </view>
      <view class="bill-list">
        <view v-for="item in billHistory" :key="item.id" class="bill-item" @click="goBillDetail(item.id)">
          <view class="left">
            <text class="month">{{ item.month }}</text>
            <text class="desc">订单{{ item.orderCount }}笔 核销{{ item.verifyCount }}笔</text>
          </view>
          <view class="right">
            <text class="amount">¥{{ item.totalFee }}</text>
            <text class="status" :class="item.status">{{ getStatusText(item.status) }}</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
      
      <view v-if="billHistory.length === 0" class="empty-state">
        <text>暂无历史账单</text>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view v-if="overview.status === 'unpaid'" class="bottom-bar">
      <view class="amount-info">
        <text class="label">待支付:</text>
        <text class="amount">¥{{ overview.totalFee }}</text>
      </view>
      <button class="pay-btn" @click="handlePay">立即支付</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface Overview {
  totalFee: number
  orderFee: number
  verifyFee: number
  otherFee: number
  status: 'unpaid' | 'paid' | 'overdue'
}

interface RateInfo {
  orderRate: number
  verifyRate: number
  settlementCycle: string
}

interface BillItem {
  id: string
  month: string
  orderCount: number
  verifyCount: number
  totalFee: number
  status: 'unpaid' | 'paid' | 'overdue'
}

const selectedMonth = ref(new Date().toISOString().slice(0, 7))
const overview = ref<Overview>({
  totalFee: 0, orderFee: 0, verifyFee: 0, otherFee: 0, status: 'paid'
})
const rateInfo = ref<RateInfo>({
  orderRate: 0, verifyRate: 0, settlementCycle: ''
})
const billHistory = ref<BillItem[]>([])

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    unpaid: '待支付',
    paid: '已支付',
    overdue: '已逾期'
  }
  return map[status] || ''
}

const onMonthChange = (e: any) => {
  selectedMonth.value = e.detail.value
}

const loadData = async () => {
  try {
    const res = await get<{
      overview: Overview
      rateInfo: RateInfo
      history: BillItem[]
    }>('/admin/billing/service-fee', {
      month: selectedMonth.value
    })
    
    if (res.code === 0) {
      overview.value = res.data.overview
      rateInfo.value = res.data.rateInfo
      billHistory.value = res.data.history || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const goBillDetail = (id: string) => {
  uni.navigateTo({ url: `/pages/admin/billing/detail?id=${id}` })
}

const handlePay = async () => {
  uni.showLoading({ title: '创建订单...' })
  try {
    const res = await post<{ payParams: any }>('/admin/billing/service-fee/pay', {
      month: selectedMonth.value
    })
    
    if (res.code === 0) {
      await uni.requestPayment({
        provider: 'wxpay',
        ...res.data.payParams
      })
      uni.showToast({ title: '支付成功', icon: 'success' })
      loadData()
    }
  } catch (e: any) {
    if (!e.errMsg?.includes('cancel')) {
      uni.showToast({ title: '支付失败', icon: 'none' })
    }
  } finally {
    uni.hideLoading()
  }
}

watch(selectedMonth, () => {
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<style lang="scss">
.service-fee-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

.overview-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  color: #fff;
  position: relative;
  
  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title {
      font-size: 30rpx;
      font-weight: bold;
    }
    
    .month-picker {
      display: flex;
      align-items: center;
      padding: 8rpx 16rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      font-size: 26rpx;
      
      .arrow {
        margin-left: 8rpx;
        font-size: 20rpx;
      }
    }
  }
  
  .overview-body {
    .main-amount {
      text-align: center;
      padding-bottom: 24rpx;
      border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
      
      .label {
        font-size: 26rpx;
        opacity: 0.9;
      }
      
      .amount-row {
        display: flex;
        align-items: baseline;
        justify-content: center;
        margin-top: 12rpx;
        
        .currency {
          font-size: 32rpx;
        }
        
        .amount {
          font-size: 64rpx;
          font-weight: bold;
          margin-left: 8rpx;
        }
      }
    }
    
    .detail-row {
      display: flex;
      justify-content: space-around;
      padding-top: 24rpx;
      
      .detail-item {
        text-align: center;
        
        .num {
          display: block;
          font-size: 30rpx;
          font-weight: bold;
        }
        
        .label {
          display: block;
          font-size: 22rpx;
          opacity: 0.8;
          margin-top: 8rpx;
        }
      }
    }
  }
  
  .pay-status {
    position: absolute;
    top: 24rpx;
    right: 24rpx;
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
    font-size: 22rpx;
    
    &.paid {
      background: rgba(76, 175, 80, 0.3);
    }
    
    &.unpaid {
      background: rgba(255, 152, 0, 0.3);
    }
    
    &.overdue {
      background: rgba(244, 67, 54, 0.3);
    }
  }
}

.rate-section, .history-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-header {
  margin-bottom: 20rpx;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
}

.rate-list {
  .rate-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      font-size: 28rpx;
      color: #666;
    }
    
    .value {
      font-size: 28rpx;
      color: #333;
      font-weight: bold;
    }
  }
}

.bill-list {
  .bill-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .left {
      flex: 1;
      
      .month {
        display: block;
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .desc {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
    
    .right {
      text-align: right;
      margin-right: 12rpx;
      
      .amount {
        display: block;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
      
      .status {
        display: block;
        font-size: 22rpx;
        margin-top: 8rpx;
        
        &.paid { color: #4caf50; }
        &.unpaid { color: #ff9800; }
        &.overdue { color: #f44336; }
      }
    }
    
    .arrow {
      font-size: 28rpx;
      color: #ccc;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 26rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .amount-info {
    .label {
      font-size: 26rpx;
      color: #666;
    }
    
    .amount {
      font-size: 40rpx;
      font-weight: bold;
      color: #ff6b35;
    }
  }
  
  .pay-btn {
    padding: 20rpx 60rpx;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: #fff;
    font-size: 30rpx;
    font-weight: bold;
    border-radius: 40rpx;
    border: none;
  }
}
</style>
