<template>
  <view class="billing-page">
    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view 
        v-for="tab in tabs" 
        :key="tab.key" 
        class="tab-item" 
        :class="{ active: currentTab === tab.key }"
        @click="currentTab = tab.key"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- Tab: 套餐管理 -->
    <view v-if="currentTab === 'package'" class="tab-content">
      <!-- 当前套餐 -->
      <view class="current-package" v-if="currentPackage">
        <view class="pkg-header">
          <text class="pkg-title">当前套餐</text>
          <text class="pkg-status" :class="{ expired: currentPackage.remainingDays <= 0 }">
            {{ currentPackage.remainingDays > 0 ? `剩余${currentPackage.remainingDays}天` : '已过期' }}
          </text>
        </view>
        <view class="pkg-body">
          <text class="pkg-name">{{ currentPackage.packageName }}</text>
          <text class="pkg-rate">服务费率: {{ (currentPackage.serviceFeeRate * 100).toFixed(2) }}%</text>
          <text class="pkg-period">有效期: {{ currentPackage.validStartDate }} ~ {{ currentPackage.validEndDate }}</text>
        </view>
      </view>
      <view class="current-package empty" v-else>
        <text class="empty-text">尚未购买套餐</text>
      </view>

      <!-- 可选套餐 -->
      <view class="section-title">选择套餐</view>
      <view class="package-list">
        <view 
          v-for="pkg in packageList" 
          :key="pkg.packageId" 
          class="package-card"
          :class="{ selected: selectedPackageId === pkg.packageId }"
          @click="selectedPackageId = pkg.packageId"
        >
          <view class="card-top">
            <text class="name">{{ pkg.packageName }}</text>
            <view class="price-row">
              <text class="currency">¥</text>
              <text class="price">{{ pkg.price }}</text>
              <text class="period">/{{ pkg.durationMonths }}个月</text>
            </view>
          </view>
          <view class="card-bottom">
            <text class="rate">服务费率: {{ (pkg.serviceFeeRate * 100).toFixed(2) }}%</text>
            <text class="features">{{ pkg.features }}</text>
          </view>
        </view>
      </view>

      <view v-if="packageList.length > 0" class="action-area">
        <button 
          class="buy-btn" 
          :disabled="!selectedPackageId" 
          @click="handlePurchasePackage"
          :loading="purchasing"
        >
          立即购买
        </button>
      </view>
    </view>

    <!-- Tab: 保证金 -->
    <view v-if="currentTab === 'deposit'" class="tab-content">
      <view class="deposit-card" :class="depositStatusClass">
        <view class="deposit-icon">{{ depositStatusIcon }}</view>
        <text class="deposit-status">{{ depositStatusText }}</text>
        <text class="deposit-amount">¥{{ depositInfo.depositAmount || 0 }}</text>
        <text v-if="depositInfo.payTime" class="deposit-time">
          缴纳时间: {{ formatTime(depositInfo.payTime) }}
        </text>
      </view>

      <view v-if="depositInfo.payStatus !== 1" class="deposit-form">
        <view class="section-title">缴纳保证金</view>
        <view class="form-item">
          <text class="label">保证金金额(元)</text>
          <input 
            v-model.number="depositAmount" 
            type="digit" 
            class="input" 
            placeholder="输入0表示免保证金"
          />
          <text class="hint">平台允许保证金为零，视商户等级而定</text>
        </view>
        <button class="pay-btn" @click="handlePayDeposit" :loading="payingDeposit">
          {{ depositAmount > 0 ? '立即缴纳' : '确认免保证金' }}
        </button>
      </view>

      <view class="refund-tips">
        <text class="title">保证金说明</text>
        <text class="content">1. 保证金可为零，由平台根据商户等级设定
2. 合作终止且无违规记录时可申请退还
3. 如需申请退还，请联系平台客服</text>
      </view>
    </view>

    <!-- Tab: 服务费 -->
    <view v-if="currentTab === 'service'" class="tab-content">
      <!-- 余额概览 -->
      <view class="balance-card">
        <view class="balance-header">
          <text class="title">账户余额</text>
        </view>
        <view class="balance-amount">
          <text class="currency">¥</text>
          <text class="amount">{{ balanceInfo.balance || '0.00' }}</text>
        </view>
        <view class="balance-detail">
          <view class="detail-item">
            <text class="num">¥{{ balanceInfo.totalRecharge || '0.00' }}</text>
            <text class="label">累计充值</text>
          </view>
          <view class="detail-item">
            <text class="num">¥{{ balanceInfo.totalConsume || '0.00' }}</text>
            <text class="label">累计消费</text>
          </view>
        </view>
      </view>

      <!-- 月度服务费 -->
      <view class="section-title">月度服务费</view>
      <view class="fee-list">
        <view v-for="item in monthlyFees" :key="item.summaryId" class="fee-item">
          <view class="left">
            <text class="month">{{ item.yearMonth }}</text>
            <text class="desc">{{ item.orderCount }}笔订单 / ¥{{ item.totalAmount }}</text>
          </view>
          <view class="right">
            <text class="amount">¥{{ item.serviceFee }}</text>
            <text class="status" :class="getDeductStatusClass(item.deductStatus)">
              {{ getDeductStatusText(item.deductStatus) }}
            </text>
          </view>
        </view>
        <view v-if="monthlyFees.length === 0" class="empty-state">
          <text>暂无服务费记录</text>
        </view>
      </view>

      <!-- 近期流水 -->
      <view v-if="balanceInfo.recentLogs && balanceInfo.recentLogs.length > 0" class="section-title">近期流水</view>
      <view v-if="balanceInfo.recentLogs && balanceInfo.recentLogs.length > 0" class="log-list">
        <view v-for="log in balanceInfo.recentLogs" :key="log.logId" class="log-item">
          <view class="left">
            <text class="type">{{ getChangeTypeText(log.changeType) }}</text>
            <text class="time">{{ log.createdAt }}</text>
            <text v-if="log.remark" class="remark">{{ log.remark }}</text>
          </view>
          <view class="right">
            <text class="amount" :class="{ plus: log.changeType === 1 || log.changeType === 3, minus: log.changeType === 2 }">
              {{ log.changeType === 2 ? '-' : '+' }}¥{{ log.amount }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get, post } from '@/api/request'

// Tab 定义
const tabs = [
  { key: 'package', label: '套餐管理' },
  { key: 'deposit', label: '保证金' },
  { key: 'service', label: '服务费' }
]
const currentTab = ref('package')

// ========== 套餐管理 ==========
interface PackageItem {
  packageId: number
  packageName: string
  packageType: number
  price: number
  durationMonths: number
  serviceFeeRate: number
  features: string
}

interface CurrentPackageInfo {
  purchaseId: number
  packageId: number
  packageName: string
  packageType: number
  pricePaid: number
  serviceFeeRate: number
  payStatus: number
  validStartDate: string
  validEndDate: string
  remainingDays: number
  features: string
}

const packageList = ref<PackageItem[]>([])
const currentPackage = ref<CurrentPackageInfo | null>(null)
const selectedPackageId = ref<number | null>(null)
const purchasing = ref(false)

const loadPackages = async () => {
  try {
    const data = await get<PackageItem[]>('/v1/merchant/billing/packages')
    packageList.value = data || []
  } catch (e) {
    console.error('加载套餐失败', e)
  }
}

const loadCurrentPackage = async () => {
  try {
    const data = await get<CurrentPackageInfo>('/v1/merchant/billing/packages/current')
    currentPackage.value = data
  } catch (e) {
    currentPackage.value = null
  }
}

const handlePurchasePackage = async () => {
  if (!selectedPackageId.value) return
  
  uni.showModal({
    title: '确认购买',
    content: '确定要购买此套餐吗？',
    success: async (res) => {
      if (res.confirm) {
        purchasing.value = true
        try {
          await post('/v1/merchant/billing/packages/purchase', {
            packageId: selectedPackageId.value
          })
          uni.showToast({ title: '购买成功', icon: 'success' })
          selectedPackageId.value = null
          loadCurrentPackage()
        } catch (e: any) {
          uni.showToast({ title: e.message || '购买失败', icon: 'none' })
        } finally {
          purchasing.value = false
        }
      }
    }
  })
}

// ========== 保证金 ==========
interface DepositInfo {
  depositId?: number
  depositAmount: number
  payStatus: number
  payTime?: string
  refundTime?: string
  refundReason?: string
}

const depositInfo = ref<DepositInfo>({ depositAmount: 0, payStatus: -1 })
const depositAmount = ref<number>(0)
const payingDeposit = ref(false)

const depositStatusClass = ref('')
const depositStatusIcon = ref('📋')
const depositStatusText = ref('')

const updateDepositStatus = () => {
  const status = depositInfo.value.payStatus
  if (status === 1) {
    depositStatusClass.value = 'paid'
    depositStatusIcon.value = '✓'
    depositStatusText.value = '已缴纳'
  } else if (status === 0) {
    depositStatusClass.value = 'unpaid'
    depositStatusIcon.value = '⏳'
    depositStatusText.value = '待支付'
  } else {
    depositStatusClass.value = 'none'
    depositStatusIcon.value = '📋'
    depositStatusText.value = '未设置'
  }
}

const loadDeposit = async () => {
  try {
    const data = await get<DepositInfo>('/v1/merchant/billing/deposit')
    depositInfo.value = data || { depositAmount: 0, payStatus: -1 }
    updateDepositStatus()
  } catch (e) {
    console.error('加载保证金失败', e)
  }
}

const handlePayDeposit = async () => {
  payingDeposit.value = true
  try {
    const data = await post<DepositInfo>('/v1/merchant/billing/deposit/pay', {
      amount: depositAmount.value || 0
    })
    depositInfo.value = data || depositInfo.value
    updateDepositStatus()
    uni.showToast({ title: depositAmount.value > 0 ? '缴纳成功' : '已确认', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  } finally {
    payingDeposit.value = false
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

// ========== 服务费 ==========
interface BalanceInfo {
  balance: number
  totalRecharge: number
  totalConsume: number
  recentLogs: BalanceLogItem[]
}

interface BalanceLogItem {
  logId: number
  changeType: number
  amount: number
  balanceBefore: number
  balanceAfter: number
  relatedOrderNo: string
  remark: string
  createdAt: string
}

interface MonthlyFeeItem {
  summaryId: number
  yearMonth: string
  orderCount: number
  totalAmount: number
  serviceFee: number
  deductStatus: number
  deductTime: string
}

const balanceInfo = ref<BalanceInfo>({ balance: 0, totalRecharge: 0, totalConsume: 0, recentLogs: [] })
const monthlyFees = ref<MonthlyFeeItem[]>([])

const loadBalance = async () => {
  try {
    const data = await get<BalanceInfo>('/v1/merchant/billing/balance')
    balanceInfo.value = data || { balance: 0, totalRecharge: 0, totalConsume: 0, recentLogs: [] }
  } catch (e) {
    console.error('加载余额失败', e)
  }
}

const loadMonthlyFees = async () => {
  try {
    const data = await get<MonthlyFeeItem[]>('/v1/merchant/billing/service-fees')
    monthlyFees.value = data || []
  } catch (e) {
    console.error('加载月度服务费失败', e)
  }
}

const getDeductStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待扣减', 1: '已扣减', 2: '余额不足' }
  return map[status] || '未知'
}

const getDeductStatusClass = (status: number) => {
  const map: Record<number, string> = { 0: 'pending', 1: 'success', 2: 'warning' }
  return map[status] || ''
}

const getChangeTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '充值', 2: '服务费扣减', 3: '退款' }
  return map[type] || '其他'
}

// Tab 切换时加载数据
watch(currentTab, (tab) => {
  if (tab === 'package') {
    loadPackages()
    loadCurrentPackage()
  } else if (tab === 'deposit') {
    loadDeposit()
  } else if (tab === 'service') {
    loadBalance()
    loadMonthlyFees()
  }
})

onMounted(() => {
  loadPackages()
  loadCurrentPackage()
})
</script>

<style lang="scss">
.billing-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;

  .tab-item {
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
        width: 60rpx;
        height: 4rpx;
        background: #ff6b35;
        border-radius: 2rpx;
      }
    }
  }
}

.tab-content {
  padding: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin: 24rpx 0 16rpx;
}

// ========== 套餐 ==========
.current-package {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-radius: 16rpx;
  padding: 24rpx;
  color: #fff;

  &.empty {
    background: #e0e0e0;
    text-align: center;
    padding: 48rpx 24rpx;

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }

  .pkg-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .pkg-title { font-size: 28rpx; opacity: 0.9; }
    .pkg-status {
      padding: 4rpx 16rpx;
      background: rgba(255,255,255,0.3);
      border-radius: 16rpx;
      font-size: 22rpx;

      &.expired { background: rgba(244,67,54,0.5); }
    }
  }

  .pkg-body {
    margin-top: 16rpx;

    .pkg-name {
      display: block;
      font-size: 36rpx;
      font-weight: bold;
      margin-bottom: 8rpx;
    }

    .pkg-rate, .pkg-period {
      display: block;
      font-size: 24rpx;
      opacity: 0.9;
      margin-top: 4rpx;
    }
  }
}

.package-list {
  .package-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    border: 2rpx solid transparent;
    transition: border-color 0.2s;

    &.selected {
      border-color: #ff6b35;
      box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.15);
    }

    .card-top {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .name { font-size: 30rpx; font-weight: bold; color: #333; }

      .price-row {
        display: flex;
        align-items: baseline;
        color: #ff6b35;

        .currency { font-size: 24rpx; }
        .price { font-size: 40rpx; font-weight: bold; margin: 0 4rpx; }
        .period { font-size: 22rpx; color: #999; }
      }
    }

    .card-bottom {
      margin-top: 12rpx;

      .rate {
        display: block;
        font-size: 24rpx;
        color: #666;
      }

      .features {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
}

.action-area {
  margin-top: 24rpx;

  .buy-btn {
    width: 100%;
    padding: 24rpx;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 48rpx;
    border: none;

    &[disabled] {
      opacity: 0.5;
    }
  }
}

// ========== 保证金 ==========
.deposit-card {
  border-radius: 20rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  color: #fff;

  &.paid { background: linear-gradient(135deg, #4caf50, #8bc34a); }
  &.unpaid { background: linear-gradient(135deg, #ff9800, #ffc107); }
  &.none { background: linear-gradient(135deg, #9e9e9e, #bdbdbd); }

  .deposit-icon {
    font-size: 60rpx;
    margin-bottom: 16rpx;
  }

  .deposit-status {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
  }

  .deposit-amount {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    margin-top: 12rpx;
  }

  .deposit-time {
    display: block;
    font-size: 24rpx;
    opacity: 0.9;
    margin-top: 12rpx;
  }
}

.deposit-form {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-top: 24rpx;

  .form-item {
    margin-bottom: 24rpx;

    .label {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-bottom: 12rpx;
    }

    .input {
      width: 100%;
      padding: 20rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 28rpx;
    }

    .hint {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
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
  margin-top: 24rpx;
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

// ========== 服务费 ==========
.balance-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 20rpx;
  padding: 24rpx;
  color: #fff;

  .balance-header .title {
    font-size: 28rpx;
    opacity: 0.9;
  }

  .balance-amount {
    display: flex;
    align-items: baseline;
    justify-content: center;
    padding: 24rpx 0;

    .currency { font-size: 32rpx; }
    .amount { font-size: 64rpx; font-weight: bold; margin-left: 8rpx; }
  }

  .balance-detail {
    display: flex;
    justify-content: space-around;
    padding-top: 16rpx;
    border-top: 1rpx solid rgba(255,255,255,0.2);

    .detail-item {
      text-align: center;

      .num { display: block; font-size: 28rpx; font-weight: bold; }
      .label { display: block; font-size: 22rpx; opacity: 0.8; margin-top: 4rpx; }
    }
  }
}

.fee-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 24rpx;

  .fee-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child { border-bottom: none; }

    .left {
      .month { display: block; font-size: 28rpx; font-weight: bold; color: #333; }
      .desc { display: block; font-size: 24rpx; color: #999; margin-top: 4rpx; }
    }

    .right {
      text-align: right;

      .amount { display: block; font-size: 30rpx; font-weight: bold; color: #333; }
      .status {
        display: block; font-size: 22rpx; margin-top: 4rpx;
        &.pending { color: #ff9800; }
        &.success { color: #4caf50; }
        &.warning { color: #f44336; }
      }
    }
  }
}

.log-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 24rpx;

  .log-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child { border-bottom: none; }

    .left {
      .type { display: block; font-size: 28rpx; color: #333; }
      .time { display: block; font-size: 22rpx; color: #999; margin-top: 4rpx; }
      .remark { display: block; font-size: 22rpx; color: #999; }
    }

    .right .amount {
      font-size: 30rpx; font-weight: bold;
      &.plus { color: #4caf50; }
      &.minus { color: #f44336; }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 48rpx;
  color: #999;
  font-size: 26rpx;
  background: #fff;
  border-radius: 16rpx;
}
</style>
