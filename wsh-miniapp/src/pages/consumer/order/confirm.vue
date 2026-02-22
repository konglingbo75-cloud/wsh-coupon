<template>
  <view class="order-confirm-page">
    <!-- 活动信息 -->
    <view class="activity-section">
      <image class="cover" :src="activity?.coverUrl" mode="aspectFill" />
      <view class="info">
        <text class="title">{{ activity?.title }}</text>
        <view class="merchant-row">
          <image class="merchant-logo" :src="activity?.merchant?.logoUrl" mode="aspectFill" />
          <text class="merchant-name">{{ activity?.merchant?.name }}</text>
        </view>
        <view class="price-row">
          <text class="price">¥{{ activity?.price }}</text>
          <text class="quantity">x{{ quantity }}</text>
        </view>
      </view>
    </view>

    <!-- 购买须知 -->
    <view class="notice-section">
      <view class="notice-item">
        <text class="icon">⏰</text>
        <text class="text">有效期：{{ activity?.validStart }} 至 {{ activity?.validEnd }}</text>
      </view>
      <view class="notice-item">
        <text class="icon">📍</text>
        <text class="text">适用门店：{{ activity?.applicableStores || '全部门店' }}</text>
      </view>
      <view class="notice-item">
        <text class="icon">📋</text>
        <text class="text">购买后不可退款，请确认后再购买</text>
      </view>
    </view>

    <!-- 手机号验证 -->
    <view class="phone-section">
      <view class="section-header">
        <text class="title">接收手机号</text>
      </view>
      <view v-if="userPhone" class="phone-display">
        <text class="phone">{{ maskPhone(userPhone) }}</text>
        <text class="change" @click="showPhoneModal = true">更换</text>
      </view>
      <view v-else class="phone-input-wrap">
        <input 
          type="number" 
          v-model="inputPhone" 
          placeholder="请输入手机号接收购买凭证" 
          maxlength="11"
        />
      </view>
    </view>

    <!-- 优惠选择 -->
    <view class="discount-section" v-if="availableCoupons.length > 0">
      <view class="section-header">
        <text class="title">优惠券</text>
      </view>
      <view class="coupon-selector" @click="showCouponModal = true">
        <text v-if="selectedCoupon" class="selected">-¥{{ selectedCoupon.discount }}</text>
        <text v-else class="placeholder">{{ availableCoupons.length }}张可用</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 金额明细 -->
    <view class="amount-section">
      <view class="amount-item">
        <text class="label">商品金额</text>
        <text class="value">¥{{ (activity?.price || 0) * quantity }}</text>
      </view>
      <view v-if="selectedCoupon" class="amount-item discount">
        <text class="label">优惠券抵扣</text>
        <text class="value">-¥{{ selectedCoupon.discount }}</text>
      </view>
      <view class="amount-item total">
        <text class="label">应付金额</text>
        <text class="value">¥{{ finalAmount }}</text>
      </view>
    </view>

    <!-- 底部支付栏 -->
    <view class="bottom-bar">
      <view class="total-section">
        <text class="label">合计:</text>
        <text class="amount">¥{{ finalAmount }}</text>
      </view>
      <button class="pay-btn" :disabled="!canPay" @click="handlePay">
        {{ paying ? '支付中...' : '确认支付' }}
      </button>
    </view>

    <!-- 优惠券选择弹窗 -->
    <view v-if="showCouponModal" class="coupon-modal" @click="showCouponModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="title">选择优惠券</text>
          <text class="close" @click="showCouponModal = false">✕</text>
        </view>
        <view class="coupon-list">
          <view 
            v-for="item in availableCoupons" 
            :key="item.id" 
            class="coupon-item"
            :class="{ selected: selectedCoupon?.id === item.id }"
            @click="selectCoupon(item)"
          >
            <view class="coupon-value">
              <text class="currency">¥</text>
              <text class="amount">{{ item.discount }}</text>
            </view>
            <view class="coupon-info">
              <text class="name">{{ item.name }}</text>
              <text class="condition">{{ item.condition }}</text>
            </view>
            <view class="check-mark" v-if="selectedCoupon?.id === item.id">✓</view>
          </view>
        </view>
        <view class="modal-footer">
          <text class="no-use" @click="selectCoupon(null)">不使用优惠券</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get, post } from '@/api/request'
import { useUserStore } from '@/store/user'
import { createOrder, requestPayment } from '@/api/order'

interface ActivityInfo {
  id: string
  title: string
  price: number
  coverUrl: string
  validStart: string
  validEnd: string
  applicableStores?: string
  merchant: {
    id: string
    name: string
    logoUrl: string
  }
}

interface CouponItem {
  id: string
  name: string
  discount: number
  condition: string
}

const userStore = useUserStore()

const activityId = ref('')
const quantity = ref(1)
const activity = ref<ActivityInfo | null>(null)
const availableCoupons = ref<CouponItem[]>([])
const selectedCoupon = ref<CouponItem | null>(null)
const showCouponModal = ref(false)
const showPhoneModal = ref(false)
const inputPhone = ref('')
const paying = ref(false)

const userPhone = computed(() => userStore.phone)

const finalAmount = computed(() => {
  const base = (activity.value?.price || 0) * quantity.value
  const discount = selectedCoupon.value?.discount || 0
  return Math.max(0, base - discount)
})

const canPay = computed(() => {
  const phone = userPhone.value || inputPhone.value
  return activity.value && phone && phone.length === 11 && !paying.value
})

const maskPhone = (phone: string) => {
  if (phone.length !== 11) return phone
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

const loadData = async () => {
  try {
    // 加载活动信息
    const activityRes = await get<{ activity: ActivityInfo }>(`/public/activities/${activityId.value}`)
    if (activityRes.code === 0) {
      activity.value = activityRes.data.activity
    }
    
    // 加载可用优惠券
    const couponRes = await get<{ list: CouponItem[] }>('/consumer/coupons/available', {
      activityId: activityId.value,
      amount: (activity.value?.price || 0) * quantity.value
    })
    if (couponRes.code === 0) {
      availableCoupons.value = couponRes.data.list || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const selectCoupon = (coupon: CouponItem | null) => {
  selectedCoupon.value = coupon
  showCouponModal.value = false
}

const handlePay = async () => {
  if (!canPay.value) return
  
  const phone = userPhone.value || inputPhone.value
  
  paying.value = true
  try {
    // 创建订单
    const orderRes = await createOrder({
      activityId: activityId.value,
      quantity: quantity.value,
      phone: phone,
      couponId: selectedCoupon.value?.id
    })
    
    if (orderRes.code !== 0) {
      uni.showToast({ title: orderRes.message || '创建订单失败', icon: 'none' })
      return
    }
    
    const orderId = orderRes.data.orderId
    
    // 发起支付
    const payRes = await requestPayment(orderId)
    
    if (payRes.code !== 0) {
      uni.showToast({ title: payRes.message || '支付失败', icon: 'none' })
      // 跳转到订单详情
      uni.redirectTo({ url: `/pages/consumer/order/detail?id=${orderId}` })
      return
    }
    
    // 调用微信支付
    try {
      await uni.requestPayment({
        provider: 'wxpay',
        ...payRes.data.payParams
      })
      
      // 支付成功
      uni.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: `/pages/consumer/order/detail?id=${orderId}` })
      }, 1500)
    } catch (payErr: any) {
      if (payErr.errMsg?.includes('cancel')) {
        uni.showToast({ title: '已取消支付', icon: 'none' })
      } else {
        uni.showToast({ title: '支付失败', icon: 'none' })
      }
      uni.redirectTo({ url: `/pages/consumer/order/detail?id=${orderId}` })
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  activityId.value = currentPage.options?.activityId || ''
  quantity.value = parseInt(currentPage.options?.quantity) || 1
  
  if (activityId.value) {
    loadData()
  }
})
</script>

<style lang="scss">
.order-confirm-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

.activity-section {
  display: flex;
  background: #fff;
  padding: 24rpx;
  margin-bottom: 16rpx;
  
  .cover {
    width: 180rpx;
    height: 180rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }
  
  .info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .title {
      font-size: 30rpx;
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
    
    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .price {
        font-size: 36rpx;
        font-weight: bold;
        color: #ff6b35;
      }
      
      .quantity {
        font-size: 28rpx;
        color: #666;
      }
    }
  }
}

.notice-section {
  background: #fff;
  padding: 24rpx;
  margin-bottom: 16rpx;
  
  .notice-item {
    display: flex;
    align-items: center;
    padding: 12rpx 0;
    
    .icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    .text {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.phone-section {
  background: #fff;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.section-header {
  margin-bottom: 20rpx;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
}

.phone-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .phone {
    font-size: 32rpx;
    color: #333;
  }
  
  .change {
    font-size: 26rpx;
    color: #ff6b35;
  }
}

.phone-input-wrap {
  input {
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
  }
}

.discount-section {
  background: #fff;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.coupon-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  
  .selected {
    font-size: 30rpx;
    font-weight: bold;
    color: #ff6b35;
  }
  
  .placeholder {
    font-size: 28rpx;
    color: #999;
  }
  
  .arrow {
    font-size: 32rpx;
    color: #ccc;
  }
}

.amount-section {
  background: #fff;
  padding: 24rpx;
  
  .amount-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    
    .label {
      font-size: 28rpx;
      color: #666;
    }
    
    .value {
      font-size: 28rpx;
      color: #333;
    }
    
    &.discount .value {
      color: #ff6b35;
    }
    
    &.total {
      margin-top: 16rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      
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
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .total-section {
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
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 40rpx;
    border: none;
    
    &[disabled] {
      background: #ccc;
    }
  }
}

.coupon-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
  
  .modal-content {
    width: 100%;
    background: #fff;
    border-radius: 24rpx 24rpx 0 0;
    max-height: 70vh;
    display: flex;
    flex-direction: column;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24rpx;
      border-bottom: 1rpx solid #f0f0f0;
      
      .title {
        font-size: 32rpx;
        font-weight: bold;
      }
      
      .close {
        font-size: 36rpx;
        color: #999;
        padding: 8rpx;
      }
    }
    
    .coupon-list {
      flex: 1;
      overflow-y: auto;
      padding: 24rpx;
      
      .coupon-item {
        display: flex;
        align-items: center;
        padding: 24rpx;
        background: #fafafa;
        border-radius: 12rpx;
        margin-bottom: 16rpx;
        position: relative;
        
        &.selected {
          background: #fff3e0;
          border: 2rpx solid #ff6b35;
        }
        
        .coupon-value {
          display: flex;
          align-items: baseline;
          margin-right: 24rpx;
          
          .currency {
            font-size: 24rpx;
            color: #ff6b35;
          }
          
          .amount {
            font-size: 48rpx;
            font-weight: bold;
            color: #ff6b35;
          }
        }
        
        .coupon-info {
          flex: 1;
          
          .name {
            display: block;
            font-size: 28rpx;
            font-weight: bold;
            color: #333;
          }
          
          .condition {
            display: block;
            font-size: 24rpx;
            color: #999;
            margin-top: 8rpx;
          }
        }
        
        .check-mark {
          width: 40rpx;
          height: 40rpx;
          background: #ff6b35;
          color: #fff;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24rpx;
        }
      }
    }
    
    .modal-footer {
      padding: 24rpx;
      padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
      text-align: center;
      border-top: 1rpx solid #f0f0f0;
      
      .no-use {
        font-size: 28rpx;
        color: #666;
      }
    }
  }
}
</style>
