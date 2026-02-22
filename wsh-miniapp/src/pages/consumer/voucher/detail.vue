<template>
  <view class="voucher-detail-page">
    <!-- 券码展示 -->
    <view class="code-section" :class="{ disabled: voucher?.status !== 'unused' }">
      <view class="qrcode-wrap">
        <image class="qrcode" :src="voucher?.qrcodeUrl" mode="aspectFit" />
      </view>
      <view class="code-info">
        <text class="code">{{ voucher?.code }}</text>
        <text class="copy" @click="copyCode">复制</text>
      </view>
      <text class="hint">{{ getCodeHint() }}</text>
    </view>

    <!-- 券信息 -->
    <view class="voucher-info-section">
      <view class="voucher-header">
        <view class="value-section">
          <text v-if="voucher?.type === 'cash'" class="currency">¥</text>
          <text class="value">{{ voucher?.value }}</text>
          <text v-if="voucher?.type === 'discount'" class="unit">折</text>
        </view>
        <view class="name-section">
          <text class="name">{{ voucher?.name }}</text>
          <text class="condition">{{ voucher?.condition }}</text>
        </view>
      </view>
      
      <view class="status-badge" :class="voucher?.status">
        {{ getStatusText(voucher?.status) }}
      </view>
    </view>

    <!-- 商户信息 -->
    <view class="merchant-section" @click="goMerchant">
      <image class="logo" :src="voucher?.merchantLogo" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ voucher?.merchantName }}</text>
        <text class="address">{{ voucher?.merchantAddress }}</text>
      </view>
      <text class="arrow">›</text>
    </view>

    <!-- 使用规则 -->
    <view class="rules-section">
      <view class="section-header">
        <text class="title">使用规则</text>
      </view>
      <view class="rule-list">
        <view class="rule-item">
          <text class="icon">⏰</text>
          <text class="text">有效期：{{ voucher?.validStart }} 至 {{ voucher?.validEnd }}</text>
        </view>
        <view class="rule-item">
          <text class="icon">📍</text>
          <text class="text">适用门店：{{ voucher?.applicableStores || '全部门店' }}</text>
        </view>
        <view class="rule-item">
          <text class="icon">🕐</text>
          <text class="text">使用时间：{{ voucher?.useTimeDesc || '营业时间内可用' }}</text>
        </view>
        <view v-if="voucher?.minConsume" class="rule-item">
          <text class="icon">💰</text>
          <text class="text">最低消费：满{{ voucher?.minConsume }}元可用</text>
        </view>
        <view class="rule-item">
          <text class="icon">📋</text>
          <text class="text">叠加规则：{{ voucher?.stackable ? '可与其他优惠叠加使用' : '不可与其他优惠叠加' }}</text>
        </view>
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="desc-section">
      <view class="section-header">
        <text class="title">使用说明</text>
      </view>
      <view class="desc-content">
        <text>{{ voucher?.description || '到店出示此券码，由商家扫码核销即可享受优惠' }}</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar" v-if="voucher?.status === 'unused'">
      <button class="action-btn" @click="goNearby">
        <text class="icon">📍</text>
        <text>附近门店</text>
      </button>
      <button class="action-btn primary" @click="showFullQrcode">
        <text class="icon">📷</text>
        <text>出示券码</text>
      </button>
    </view>

    <!-- 全屏二维码弹窗 -->
    <view v-if="showQrcodeModal" class="qrcode-modal" @click="showQrcodeModal = false">
      <view class="modal-content">
        <view class="modal-header">
          <text class="title">{{ voucher?.name }}</text>
          <text class="close" @click.stop="showQrcodeModal = false">✕</text>
        </view>
        <view class="qrcode-section">
          <image class="big-qrcode" :src="voucher?.qrcodeUrl" mode="aspectFit" />
          <text class="code">{{ voucher?.code }}</text>
        </view>
        <view class="brightness-hint">
          <text>已自动调高屏幕亮度</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getVoucherDetail } from '@/api/voucher'

interface VoucherDetail {
  id: string
  code: string
  qrcodeUrl: string
  name: string
  type: 'cash' | 'discount' | 'gift'
  value: number
  condition: string
  merchantId: string
  merchantName: string
  merchantLogo: string
  merchantAddress: string
  validStart: string
  validEnd: string
  applicableStores?: string
  useTimeDesc?: string
  minConsume?: number
  stackable: boolean
  description?: string
  status: 'unused' | 'used' | 'expired'
}

const voucherId = ref('')
const voucher = ref<VoucherDetail | null>(null)
const showQrcodeModal = ref(false)
let originalBrightness = 0.5

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    unused: '可使用',
    used: '已使用',
    expired: '已过期'
  }
  return map[status || ''] || ''
}

const getCodeHint = () => {
  if (voucher.value?.status === 'unused') {
    return '请出示此码给商家扫描'
  } else if (voucher.value?.status === 'used') {
    return '此券已使用'
  } else {
    return '此券已过期'
  }
}

const loadData = async () => {
  try {
    const res = await getVoucherDetail(voucherId.value)
    if (res.code === 0) {
      voucher.value = res.data
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const copyCode = () => {
  if (!voucher.value?.code) return
  uni.setClipboardData({
    data: voucher.value.code,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

const goMerchant = () => {
  if (voucher.value?.merchantId) {
    uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${voucher.value.merchantId}` })
  }
}

const goNearby = () => {
  if (voucher.value?.merchantId) {
    uni.navigateTo({ url: `/pages/consumer/activity/nearby?merchantId=${voucher.value.merchantId}` })
  }
}

const showFullQrcode = async () => {
  // 保存当前亮度并调高
  try {
    const { value } = await uni.getScreenBrightness()
    originalBrightness = value
    await uni.setScreenBrightness({ value: 1 })
  } catch (e) {
    // 忽略亮度调节错误
  }
  
  showQrcodeModal.value = true
}

const restoreBrightness = async () => {
  try {
    await uni.setScreenBrightness({ value: originalBrightness })
  } catch (e) {
    // 忽略
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  voucherId.value = currentPage.options?.id || ''
  if (voucherId.value) {
    loadData()
  }
})

onUnmounted(() => {
  if (showQrcodeModal.value) {
    restoreBrightness()
  }
})
</script>

<style lang="scss">
.voucher-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

.code-section {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  margin: 24rpx;
  padding: 40rpx;
  border-radius: 20rpx;
  text-align: center;
  
  &.disabled {
    background: linear-gradient(135deg, #999, #bbb);
  }
  
  .qrcode-wrap {
    background: #fff;
    padding: 24rpx;
    border-radius: 16rpx;
    display: inline-block;
    
    .qrcode {
      width: 280rpx;
      height: 280rpx;
    }
  }
  
  .code-info {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 24rpx;
    
    .code {
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
      letter-spacing: 4rpx;
    }
    
    .copy {
      margin-left: 16rpx;
      padding: 8rpx 16rpx;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 16rpx;
      font-size: 24rpx;
      color: #fff;
    }
  }
  
  .hint {
    display: block;
    margin-top: 16rpx;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.9);
  }
}

.voucher-info-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .voucher-header {
    display: flex;
    align-items: center;
    
    .value-section {
      display: flex;
      align-items: baseline;
      margin-right: 20rpx;
      
      .currency {
        font-size: 28rpx;
        color: #ff6b35;
      }
      
      .value {
        font-size: 56rpx;
        font-weight: bold;
        color: #ff6b35;
      }
      
      .unit {
        font-size: 24rpx;
        color: #ff6b35;
      }
    }
    
    .name-section {
      .name {
        display: block;
        font-size: 32rpx;
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
  }
  
  .status-badge {
    padding: 12rpx 24rpx;
    border-radius: 24rpx;
    font-size: 24rpx;
    
    &.unused {
      background: #e8f5e9;
      color: #4caf50;
    }
    
    &.used, &.expired {
      background: #f5f5f5;
      color: #999;
    }
  }
}

.merchant-section {
  display: flex;
  align-items: center;
  background: #fff;
  margin: 0 24rpx 24rpx;
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

.rules-section, .desc-section {
  background: #fff;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.section-header {
  margin-bottom: 20rpx;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
}

.rule-list {
  .rule-item {
    display: flex;
    align-items: flex-start;
    padding: 12rpx 0;
    
    .icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    .text {
      flex: 1;
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
    }
  }
}

.desc-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    background: #f5f5f5;
    color: #666;
    border: none;
    margin-right: 20rpx;
    
    &:last-child {
      margin-right: 0;
    }
    
    &.primary {
      background: linear-gradient(135deg, #ff6b35, #f7931e);
      color: #fff;
    }
    
    .icon {
      margin-right: 8rpx;
    }
  }
}

.qrcode-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .modal-content {
    width: 80%;
    background: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24rpx;
      border-bottom: 1rpx solid #f0f0f0;
      
      .title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
      
      .close {
        font-size: 36rpx;
        color: #999;
        padding: 8rpx;
      }
    }
    
    .qrcode-section {
      padding: 48rpx;
      text-align: center;
      
      .big-qrcode {
        width: 400rpx;
        height: 400rpx;
      }
      
      .code {
        display: block;
        margin-top: 24rpx;
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        letter-spacing: 4rpx;
      }
    }
    
    .brightness-hint {
      padding: 20rpx;
      background: #fff3e0;
      text-align: center;
      
      text {
        font-size: 24rpx;
        color: #f7931e;
      }
    }
  }
}
</style>
