<template>
  <view class="groupbuy-detail">
    <!-- 加载中 -->
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
    
    <template v-else-if="detail">
      <!-- 状态头部 -->
      <view class="status-header" :class="statusClass">
        <view class="status-icon">{{ statusIcon }}</view>
        <view class="status-info">
          <text class="status-text">{{ detail.statusName }}</text>
          <text class="status-desc">{{ statusDesc }}</text>
        </view>
        <view class="countdown" v-if="detail.status === 0 && remainingTime > 0">
          <text>剩余 {{ formatTime(remainingTime) }}</text>
        </view>
      </view>
      
      <!-- 活动信息 -->
      <view class="activity-card">
        <image class="cover" :src="detail.coverImage || '/static/default-activity.png'" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ detail.activityName }}</text>
          <text class="merchant">{{ detail.merchantName }}</text>
          <view class="price-row">
            <text class="price">拼团价 ¥{{ detail.groupPrice }}</text>
            <text class="original" v-if="detail.originalPrice">¥{{ detail.originalPrice }}</text>
          </view>
        </view>
      </view>
      
      <!-- 拼团进度 -->
      <view class="progress-card">
        <view class="progress-header">
          <text class="title">拼团进度</text>
          <text class="count">还差 <text class="highlight">{{ detail.remainingMembers }}</text> 人成团</text>
        </view>
        
        <!-- 参与者头像 -->
        <view class="participants">
          <view 
            class="participant" 
            v-for="(p, index) in detail.participants" 
            :key="p.userId"
          >
            <image class="avatar" :src="p.avatarUrl || '/static/default-avatar.png'" />
            <view class="initiator-tag" v-if="p.isInitiator">团长</view>
          </view>
          <!-- 空位 -->
          <view 
            class="participant empty" 
            v-for="i in emptySlots" 
            :key="'empty-' + i"
          >
            <text class="question">?</text>
          </view>
        </view>
        
        <!-- 进度条 -->
        <view class="progress-bar">
          <view class="filled" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <view class="progress-text">
          <text>{{ detail.currentMembers }}/{{ detail.requiredMembers }} 人</text>
        </view>
      </view>
      
      <!-- 拼团说明 -->
      <view class="rules-card">
        <view class="rule-item">
          <text class="icon">1</text>
          <text class="text">发起或参与拼团，支付成功后锁定名额</text>
        </view>
        <view class="rule-item">
          <text class="icon">2</text>
          <text class="text">邀请好友参团，人满即成团</text>
        </view>
        <view class="rule-item">
          <text class="icon">3</text>
          <text class="text">拼团超时未成团，自动全额退款</text>
        </view>
      </view>
    </template>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="detail">
      <!-- 拼团中 -->
      <template v-if="detail.status === 0">
        <!-- 已参与未支付 -->
        <template v-if="detail.hasJoined">
          <view class="price-info">
            <text class="label">需支付</text>
            <text class="price">¥{{ detail.groupPrice }}</text>
          </view>
          <button class="btn pay" @tap="handlePay">立即支付</button>
        </template>
        <!-- 未参与 -->
        <template v-else>
          <button class="btn join" @tap="handleJoin">参与拼团</button>
        </template>
        <!-- 分享按钮 -->
        <button class="btn share" open-type="share">
          <text>邀请好友</text>
        </button>
      </template>
      
      <!-- 已成团 -->
      <template v-else-if="detail.status === 1">
        <button class="btn success" disabled>拼团成功</button>
      </template>
      
      <!-- 已失败/已取消 -->
      <template v-else>
        <button class="btn disabled" disabled>{{ detail.statusName }}</button>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { getGroupDetail, joinGroup, requestGroupPayment, type GroupDetail } from '@/api/groupbuy'

const loading = ref(true)
const detail = ref<GroupDetail | null>(null)
const remainingTime = ref(0)
let countdownTimer: number | null = null

// 状态样式
const statusClass = computed(() => {
  if (!detail.value) return ''
  switch (detail.value.status) {
    case 0: return 'pending'
    case 1: return 'success'
    case 2: return 'failed'
    case 3: return 'cancelled'
    default: return ''
  }
})

// 状态图标
const statusIcon = computed(() => {
  if (!detail.value) return ''
  switch (detail.value.status) {
    case 0: return '⏳'
    case 1: return '✓'
    case 2: return '✗'
    case 3: return '✗'
    default: return ''
  }
})

// 状态描述
const statusDesc = computed(() => {
  if (!detail.value) return ''
  switch (detail.value.status) {
    case 0: return `还差${detail.value.remainingMembers}人，快邀请好友参团吧`
    case 1: return '恭喜！拼团已成功'
    case 2: return '很遗憾，拼团未能在规定时间内成团'
    case 3: return '拼团已取消'
    default: return ''
  }
})

// 空位数量
const emptySlots = computed(() => {
  if (!detail.value) return 0
  return Math.max(0, detail.value.requiredMembers - detail.value.currentMembers)
})

// 进度百分比
const progressPercent = computed(() => {
  if (!detail.value) return 0
  return Math.round((detail.value.currentMembers / detail.value.requiredMembers) * 100)
})

// 格式化时间
function formatTime(seconds: number): string {
  if (seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 加载数据
async function loadDetail(groupOrderId: number) {
  loading.value = true
  try {
    detail.value = await getGroupDetail(groupOrderId)
    remainingTime.value = detail.value.remainingSeconds
    startCountdown()
  } catch (err) {
    console.error('加载拼团详情失败', err)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 开始倒计时
function startCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  if (remainingTime.value > 0) {
    countdownTimer = setInterval(() => {
      remainingTime.value--
      if (remainingTime.value <= 0) {
        clearInterval(countdownTimer!)
        // 刷新状态
        if (detail.value) {
          loadDetail(detail.value.groupOrderId)
        }
      }
    }, 1000) as unknown as number
  }
}

// 参与拼团
async function handleJoin() {
  if (!detail.value) return
  
  try {
    uni.showLoading({ title: '加入中...' })
    detail.value = await joinGroup(detail.value.groupOrderId)
    uni.hideLoading()
    uni.showToast({ title: '加入成功', icon: 'success' })
  } catch (err: any) {
    uni.hideLoading()
    uni.showToast({ title: err.message || '加入失败', icon: 'none' })
  }
}

// 支付
async function handlePay() {
  if (!detail.value) return
  
  try {
    uni.showLoading({ title: '发起支付...' })
    const payParams = await requestGroupPayment(detail.value.groupOrderId)
    uni.hideLoading()
    
    // 调用微信支付
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.package,
      signType: payParams.signType as 'MD5' | 'HMAC-SHA256' | 'RSA',
      paySign: payParams.paySign,
      success: () => {
        uni.showToast({ title: '支付成功', icon: 'success' })
        // 刷新数据
        loadDetail(detail.value!.groupOrderId)
      },
      fail: (err) => {
        if (err.errMsg !== 'requestPayment:fail cancel') {
          uni.showToast({ title: '支付失败', icon: 'none' })
        }
      }
    })
  } catch (err: any) {
    uni.hideLoading()
    uni.showToast({ title: err.message || '发起支付失败', icon: 'none' })
  }
}

// 分享
onShareAppMessage(() => {
  if (!detail.value) {
    return { title: '快来参与拼团吧', path: '/pages/index/index' }
  }
  return {
    title: `${detail.value.activityName} 仅需¥${detail.value.groupPrice}，快来拼团！`,
    path: `/pages/consumer/groupbuy/detail?id=${detail.value.groupOrderId}`,
    imageUrl: detail.value.coverImage
  }
})

onLoad((options) => {
  if (options?.id) {
    loadDetail(parseInt(options.id))
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style lang="scss" scoped>
.groupbuy-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 140rpx;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 100rpx;
  color: #999;
}

.status-header {
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  
  &.pending { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  &.success { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
  &.failed { background: linear-gradient(135deg, #636363 0%, #a2ab58 100%); }
  &.cancelled { background: linear-gradient(135deg, #636363 0%, #a2ab58 100%); }
  
  .status-icon {
    width: 80rpx;
    height: 80rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin-right: 24rpx;
  }
  
  .status-info {
    flex: 1;
    
    .status-text {
      display: block;
      font-size: 36rpx;
      font-weight: 600;
      margin-bottom: 8rpx;
    }
    
    .status-desc {
      font-size: 26rpx;
      opacity: 0.9;
    }
  }
  
  .countdown {
    background: rgba(255,255,255,0.2);
    padding: 12rpx 20rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
  }
}

.activity-card {
  display: flex;
  margin: 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .cover {
    width: 160rpx;
    height: 160rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }
  
  .info {
    flex: 1;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .name {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
    }
    
    .merchant {
      font-size: 26rpx;
      color: #999;
    }
    
    .price-row {
      display: flex;
      align-items: baseline;
      gap: 12rpx;
      
      .price {
        font-size: 32rpx;
        color: #e53935;
        font-weight: 600;
      }
      
      .original {
        font-size: 24rpx;
        color: #999;
        text-decoration: line-through;
      }
    }
  }
}

.progress-card {
  margin: 0 24rpx 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
    
    .title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
    
    .count {
      font-size: 26rpx;
      color: #999;
      
      .highlight {
        color: #e53935;
        font-weight: 600;
      }
    }
  }
  
  .participants {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;
    flex-wrap: wrap;
    
    .participant {
      position: relative;
      
      .avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
      }
      
      .initiator-tag {
        position: absolute;
        bottom: -8rpx;
        left: 50%;
        transform: translateX(-50%);
        background: #ff6b6b;
        color: #fff;
        font-size: 18rpx;
        padding: 2rpx 10rpx;
        border-radius: 4rpx;
      }
      
      &.empty {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        background: #f5f5f5;
        border: 4rpx dashed #ddd;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .question {
          font-size: 36rpx;
          color: #ccc;
        }
      }
    }
  }
  
  .progress-bar {
    height: 12rpx;
    background: #f0f0f0;
    border-radius: 6rpx;
    overflow: hidden;
    
    .filled {
      height: 100%;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 6rpx;
      transition: width 0.3s ease;
    }
  }
  
  .progress-text {
    text-align: center;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.rules-card {
  margin: 0 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .rule-item {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .icon {
      width: 40rpx;
      height: 40rpx;
      background: #667eea;
      color: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      flex-shrink: 0;
    }
    
    .text {
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
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
  gap: 20rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  
  .price-info {
    .label {
      font-size: 24rpx;
      color: #999;
    }
    .price {
      font-size: 36rpx;
      color: #e53935;
      font-weight: 600;
    }
  }
  
  .btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    font-size: 30rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    
    &.pay {
      background: linear-gradient(135deg, #e53935 0%, #ff6b6b 100%);
      color: #fff;
    }
    
    &.join {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
    
    &.share {
      background: #fff;
      border: 2rpx solid #667eea;
      color: #667eea;
    }
    
    &.success {
      background: #11998e;
      color: #fff;
    }
    
    &.disabled {
      background: #ccc;
      color: #fff;
    }
  }
}
</style>
