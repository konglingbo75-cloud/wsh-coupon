<template>
  <view class="order-detail-page">
    <!-- 订单状态 -->
    <view class="status-section" :class="order?.status">
      <text class="status-text">{{ getStatusText(order?.status) }}</text>
      <text v-if="order?.status === 'pending'" class="countdown">
        剩余 {{ countdown }} 支付
      </text>
      <text v-else class="status-desc">{{ getStatusDesc(order?.status) }}</text>
    </view>

    <!-- 商户信息 -->
    <view class="merchant-section" @click="goMerchant">
      <image class="logo" :src="order?.merchantLogo" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ order?.merchantName }}</text>
        <text class="address">{{ order?.merchantAddress }}</text>
      </view>
      <text class="arrow">›</text>
    </view>

    <!-- 商品信息 -->
    <view class="product-section">
      <view class="section-header">
        <text class="title">商品信息</text>
      </view>
      <view class="product-card">
        <image class="cover" :src="order?.coverUrl" mode="aspectFill" />
        <view class="info">
          <text class="title">{{ order?.title }}</text>
          <text class="spec">{{ order?.spec }}</text>
          <view class="price-row">
            <text class="price">¥{{ order?.price }}</text>
            <text class="quantity">x{{ order?.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 券码信息（已支付状态） -->
    <view v-if="order?.status === 'paid' || order?.status === 'used'" class="voucher-section">
      <view class="section-header">
        <text class="title">使用凭证</text>
      </view>
      <view class="voucher-card" v-for="(voucher, index) in order?.vouchers" :key="index">
        <view class="voucher-code">
          <text class="code">{{ voucher.code }}</text>
          <text class="copy" @click="copyCode(voucher.code)">复制</text>
        </view>
        <view class="qrcode-section" @click="showBigQrcode(voucher.qrcodeUrl)">
          <image class="qrcode" :src="voucher.qrcodeUrl" mode="aspectFit" />
          <text class="hint">点击放大</text>
        </view>
        <view class="voucher-status" :class="voucher.status">
          {{ voucher.status === 'used' ? '已使用' : '待使用' }}
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-section">
      <view class="section-header">
        <text class="title">订单信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">订单编号</text>
          <view class="value-wrap">
            <text class="value">{{ order?.orderNo }}</text>
            <text class="copy" @click="copyCode(order?.orderNo)">复制</text>
          </view>
        </view>
        <view class="info-item">
          <text class="label">下单时间</text>
          <text class="value">{{ order?.createTime }}</text>
        </view>
        <view v-if="order?.payTime" class="info-item">
          <text class="label">支付时间</text>
          <text class="value">{{ order?.payTime }}</text>
        </view>
        <view v-if="order?.useTime" class="info-item">
          <text class="label">使用时间</text>
          <text class="value">{{ order?.useTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">接收手机</text>
          <text class="value">{{ order?.phone }}</text>
        </view>
      </view>
    </view>

    <!-- 金额明细 -->
    <view class="amount-section">
      <view class="section-header">
        <text class="title">金额明细</text>
      </view>
      <view class="amount-list">
        <view class="amount-item">
          <text class="label">商品金额</text>
          <text class="value">¥{{ order?.originalAmount }}</text>
        </view>
        <view v-if="order?.discountAmount" class="amount-item discount">
          <text class="label">优惠券抵扣</text>
          <text class="value">-¥{{ order?.discountAmount }}</text>
        </view>
        <view class="amount-item total">
          <text class="label">实付金额</text>
          <text class="value">¥{{ order?.totalAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="showBottomBar">
      <button v-if="order?.status === 'pending'" class="action-btn" @click="handleCancel">取消订单</button>
      <button v-if="order?.status === 'pending'" class="action-btn primary" @click="handlePay">立即支付</button>
      <button v-if="order?.status === 'completed'" class="action-btn primary" @click="handleReview">去评价</button>
      <button v-if="order?.status === 'cancelled'" class="action-btn primary" @click="handleRebuy">再次购买</button>
    </view>

    <!-- 二维码弹窗 -->
    <view v-if="showQrcodeModal" class="qrcode-modal" @click="showQrcodeModal = false">
      <view class="modal-content" @click.stop>
        <image class="big-qrcode" :src="currentQrcodeUrl" mode="aspectFit" />
        <text class="hint">出示此码给商家扫描核销</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { get } from '@/api/request'
import { cancelOrder, requestPayment } from '@/api/order'

interface VoucherInfo {
  code: string
  qrcodeUrl: string
  status: 'unused' | 'used'
}

interface OrderDetail {
  id: string
  orderNo: string
  status: 'pending' | 'paid' | 'used' | 'completed' | 'cancelled' | 'refunded'
  merchantId: string
  merchantName: string
  merchantLogo: string
  merchantAddress: string
  title: string
  coverUrl: string
  spec: string
  price: number
  quantity: number
  originalAmount: number
  discountAmount?: number
  totalAmount: number
  phone: string
  createTime: string
  payTime?: string
  useTime?: string
  expireTime?: string
  vouchers?: VoucherInfo[]
}

const orderId = ref('')
const order = ref<OrderDetail | null>(null)
const showQrcodeModal = ref(false)
const currentQrcodeUrl = ref('')
const countdown = ref('00:00')
let countdownTimer: any = null

const showBottomBar = computed(() => {
  return order.value && ['pending', 'completed', 'cancelled'].includes(order.value.status)
})

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    pending: '待支付',
    paid: '待使用',
    used: '已使用',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[status || ''] || ''
}

const getStatusDesc = (status?: string) => {
  const map: Record<string, string> = {
    paid: '请在有效期内使用',
    used: '感谢您的购买',
    completed: '期待您的再次光临',
    cancelled: '订单已取消',
    refunded: '退款已原路返回'
  }
  return map[status || ''] || ''
}

const loadData = async () => {
  try {
    const res = await get<{ order: OrderDetail }>(`/consumer/orders/${orderId.value}`)
    if (res.code === 0) {
      order.value = res.data.order
      if (order.value?.status === 'pending' && order.value.expireTime) {
        startCountdown(order.value.expireTime)
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const startCountdown = (expireTime: string) => {
  const updateCountdown = () => {
    const now = Date.now()
    const expire = new Date(expireTime).getTime()
    const diff = expire - now
    
    if (diff <= 0) {
      countdown.value = '00:00'
      clearInterval(countdownTimer)
      // 刷新订单状态
      loadData()
      return
    }
    
    const minutes = Math.floor(diff / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)
    countdown.value = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }
  
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

const copyCode = (code?: string) => {
  if (!code) return
  uni.setClipboardData({
    data: code,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

const showBigQrcode = (url: string) => {
  currentQrcodeUrl.value = url
  showQrcodeModal.value = true
}

const goMerchant = () => {
  if (order.value?.merchantId) {
    uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${order.value.merchantId}` })
  }
}

const handleCancel = () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await cancelOrder(orderId.value)
          if (result.code === 0) {
            uni.showToast({ title: '已取消', icon: 'success' })
            loadData()
          }
        } catch (e) {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

const handlePay = async () => {
  try {
    const res = await requestPayment(orderId.value)
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
  }
}

const handleReview = () => {
  uni.navigateTo({ url: `/pages/consumer/order/review?orderId=${orderId.value}` })
}

const handleRebuy = () => {
  // 跳转到活动详情重新购买
  uni.navigateTo({ url: `/pages/consumer/activity/detail?id=${order.value?.id}` })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  orderId.value = currentPage.options?.id || ''
  if (orderId.value) {
    loadData()
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style lang="scss">
.order-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

.status-section {
  padding: 40rpx;
  text-align: center;
  color: #fff;
  
  &.pending { background: linear-gradient(135deg, #ff9800, #ffc107); }
  &.paid { background: linear-gradient(135deg, #4caf50, #8bc34a); }
  &.used, &.completed { background: linear-gradient(135deg, #607d8b, #90a4ae); }
  &.cancelled, &.refunded { background: linear-gradient(135deg, #9e9e9e, #bdbdbd); }
  
  .status-text {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
  }
  
  .countdown {
    display: block;
    font-size: 28rpx;
    margin-top: 12rpx;
  }
  
  .status-desc {
    display: block;
    font-size: 26rpx;
    margin-top: 12rpx;
    opacity: 0.9;
  }
}

.merchant-section {
  display: flex;
  align-items: center;
  background: #fff;
  margin: 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  
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
    
    .address {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
  
  .arrow {
    font-size: 32rpx;
    color: #ccc;
  }
}

.product-section, .voucher-section, .order-info-section, .amount-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.section-header {
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
}

.product-card {
  display: flex;
  
  .cover {
    width: 160rpx;
    height: 160rpx;
    border-radius: 12rpx;
  }
  
  .info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .title {
      font-size: 28rpx;
      color: #333;
    }
    
    .spec {
      font-size: 24rpx;
      color: #999;
    }
    
    .price-row {
      display: flex;
      justify-content: space-between;
      
      .price {
        font-size: 30rpx;
        font-weight: bold;
        color: #ff6b35;
      }
      
      .quantity {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}

.voucher-card {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fafafa;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .voucher-code {
    flex: 1;
    
    .code {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      letter-spacing: 4rpx;
    }
    
    .copy {
      display: inline-block;
      margin-top: 8rpx;
      font-size: 24rpx;
      color: #ff6b35;
    }
  }
  
  .qrcode-section {
    text-align: center;
    
    .qrcode {
      width: 120rpx;
      height: 120rpx;
    }
    
    .hint {
      display: block;
      font-size: 20rpx;
      color: #999;
    }
  }
  
  .voucher-status {
    position: absolute;
    top: 16rpx;
    right: 16rpx;
    padding: 4rpx 12rpx;
    font-size: 22rpx;
    border-radius: 12rpx;
    
    &.unused {
      background: #e8f5e9;
      color: #4caf50;
    }
    
    &.used {
      background: #f5f5f5;
      color: #999;
    }
  }
}

.info-list {
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    
    .label {
      font-size: 26rpx;
      color: #999;
    }
    
    .value-wrap {
      display: flex;
      align-items: center;
      
      .value {
        font-size: 26rpx;
        color: #333;
      }
      
      .copy {
        margin-left: 16rpx;
        font-size: 24rpx;
        color: #ff6b35;
      }
    }
    
    .value {
      font-size: 26rpx;
      color: #333;
    }
  }
}

.amount-list {
  .amount-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    
    .label {
      font-size: 26rpx;
      color: #666;
    }
    
    .value {
      font-size: 26rpx;
      color: #333;
    }
    
    &.discount .value {
      color: #ff6b35;
    }
    
    &.total {
      margin-top: 16rpx;
      padding-top: 16rpx;
      border-top: 1rpx solid #f5f5f5;
      
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
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .action-btn {
    margin-left: 20rpx;
    padding: 16rpx 40rpx;
    font-size: 28rpx;
    border-radius: 32rpx;
    background: #f5f5f5;
    color: #666;
    border: none;
    
    &.primary {
      background: linear-gradient(135deg, #ff6b35, #f7931e);
      color: #fff;
    }
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
    
    .hint {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-top: 24rpx;
    }
  }
}
</style>
