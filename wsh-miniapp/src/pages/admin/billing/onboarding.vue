<template>
  <view class="billing-page">
    <!-- 入驻费状态 -->
    <view class="status-card" :class="billingInfo?.status">
      <view class="status-icon">
        {{ getStatusIcon(billingInfo?.status) }}
      </view>
      <text class="status-text">{{ getStatusText(billingInfo?.status) }}</text>
      <text class="status-desc">{{ getStatusDesc(billingInfo?.status) }}</text>
    </view>

    <!-- 费用明细 -->
    <view class="fee-section">
      <view class="section-header">
        <text class="title">费用明细</text>
      </view>
      <view class="fee-list">
        <view class="fee-item">
          <text class="label">入驻费</text>
          <text class="value">¥{{ billingInfo?.onboardingFee || 0 }}</text>
        </view>
        <view class="fee-item">
          <text class="label">保证金</text>
          <text class="value">¥{{ billingInfo?.deposit || 0 }}</text>
        </view>
        <view class="fee-item total">
          <text class="label">合计</text>
          <text class="value">¥{{ (billingInfo?.onboardingFee || 0) + (billingInfo?.deposit || 0) }}</text>
        </view>
      </view>
    </view>

    <!-- 支付记录 -->
    <view v-if="paymentRecords.length > 0" class="record-section">
      <view class="section-header">
        <text class="title">支付记录</text>
      </view>
      <view class="record-list">
        <view v-for="item in paymentRecords" :key="item.id" class="record-item">
          <view class="info">
            <text class="type">{{ item.type === 'onboarding' ? '入驻费' : '保证金' }}</text>
            <text class="time">{{ item.payTime }}</text>
          </view>
          <view class="right">
            <text class="amount">¥{{ item.amount }}</text>
            <text class="status success">已支付</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view v-if="billingInfo?.status === 'unpaid'" class="action-section">
      <text class="tips">请在7天内完成支付，逾期将影响商户正常运营</text>
      <button class="pay-btn" @click="handlePay">立即支付</button>
    </view>

    <!-- 退款说明 -->
    <view class="refund-tips">
      <text class="title">退款说明</text>
      <text class="content">
        1. 入驻费为平台服务费，不予退还\n
        2. 保证金在合作终止且无违规记录时可申请退还\n
        3. 如需申请退出，请联系平台客服
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface BillingInfo {
  status: 'unpaid' | 'paid' | 'overdue'
  onboardingFee: number
  deposit: number
  expireDate?: string
}

interface PaymentRecord {
  id: string
  type: 'onboarding' | 'deposit'
  amount: number
  payTime: string
}

const billingInfo = ref<BillingInfo | null>(null)
const paymentRecords = ref<PaymentRecord[]>([])

const getStatusIcon = (status?: string) => {
  const map: Record<string, string> = {
    unpaid: '⏳',
    paid: '✓',
    overdue: '⚠️'
  }
  return map[status || ''] || '📋'
}

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    unpaid: '待支付',
    paid: '已支付',
    overdue: '已逾期'
  }
  return map[status || ''] || ''
}

const getStatusDesc = (status?: string) => {
  if (status === 'unpaid' && billingInfo.value?.expireDate) {
    return `请在 ${billingInfo.value.expireDate} 前完成支付`
  }
  const map: Record<string, string> = {
    paid: '您的入驻费用已支付完成',
    overdue: '支付已逾期，请尽快处理'
  }
  return map[status || ''] || ''
}

const loadBillingInfo = async () => {
  try {
    const res = await get<{
      billingInfo: BillingInfo
      records: PaymentRecord[]
    }>('/admin/billing/onboarding')
    
    if (res.code === 0) {
      billingInfo.value = res.data.billingInfo
      paymentRecords.value = res.data.records || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const handlePay = async () => {
  uni.showLoading({ title: '创建订单...' })
  try {
    const res = await post<{ payParams: any }>('/admin/billing/onboarding/pay')
    
    if (res.code === 0) {
      await uni.requestPayment({
        provider: 'wxpay',
        ...res.data.payParams
      })
      uni.showToast({ title: '支付成功', icon: 'success' })
      loadBillingInfo()
    }
  } catch (e: any) {
    if (!e.errMsg?.includes('cancel')) {
      uni.showToast({ title: '支付失败', icon: 'none' })
    }
  } finally {
    uni.hideLoading()
  }
}

onMounted(() => {
  loadBillingInfo()
})
</script>

<style lang="scss">
.billing-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.status-card {
  margin: 24rpx;
  padding: 48rpx 32rpx;
  border-radius: 20rpx;
  text-align: center;
  color: #fff;
  
  &.unpaid {
    background: linear-gradient(135deg, #ff9800, #ffc107);
  }
  
  &.paid {
    background: linear-gradient(135deg, #4caf50, #8bc34a);
  }
  
  &.overdue {
    background: linear-gradient(135deg, #f44336, #ff5722);
  }
  
  .status-icon {
    width: 100rpx;
    height: 100rpx;
    margin: 0 auto 20rpx;
    font-size: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .status-text {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
  }
  
  .status-desc {
    display: block;
    font-size: 26rpx;
    margin-top: 12rpx;
    opacity: 0.9;
  }
}

.fee-section, .record-section {
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

.fee-list {
  .fee-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    &.total {
      margin-top: 16rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      border-bottom: none;
      
      .label {
        font-weight: bold;
        color: #333;
      }
      
      .value {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff6b35;
      }
    }
    
    .label {
      font-size: 28rpx;
      color: #666;
    }
    
    .value {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.record-list {
  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info {
      .type {
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
    
    .right {
      text-align: right;
      
      .amount {
        display: block;
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .status {
        display: block;
        font-size: 22rpx;
        margin-top: 8rpx;
        
        &.success {
          color: #4caf50;
        }
      }
    }
  }
}

.action-section {
  margin: 0 24rpx;
  text-align: center;
  
  .tips {
    display: block;
    font-size: 24rpx;
    color: #ff9800;
    margin-bottom: 24rpx;
  }
  
  .pay-btn {
    width: 100%;
    padding: 24rpx;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 48rpx;
    border: none;
  }
}

.refund-tips {
  margin: 24rpx;
  padding: 24rpx;
  background: #fffbf5;
  border-radius: 12rpx;
  
  .title {
    display: block;
    font-size: 26rpx;
    font-weight: bold;
    color: #f7931e;
    margin-bottom: 12rpx;
  }
  
  .content {
    font-size: 24rpx;
    color: #666;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}
</style>
