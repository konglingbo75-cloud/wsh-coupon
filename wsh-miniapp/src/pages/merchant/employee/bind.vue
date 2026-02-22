<template>
  <view class="bind-page">
    <!-- 绑定状态 -->
    <view v-if="bindInfo" class="bind-status">
      <view class="status-icon success">✓</view>
      <text class="status-text">已绑定商户</text>
      <view class="merchant-card">
        <image class="logo" :src="bindInfo.merchantLogo" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ bindInfo.merchantName }}</text>
          <text class="role">{{ bindInfo.role === 'staff' ? '员工' : '管理员' }}</text>
        </view>
      </view>
      <view class="bind-time">
        <text>绑定时间: {{ bindInfo.bindTime }}</text>
      </view>
      <button class="unbind-btn" @click="handleUnbind">解除绑定</button>
    </view>

    <!-- 未绑定状态 -->
    <view v-else class="unbind-status">
      <view class="guide-section">
        <text class="title">员工绑定</text>
        <text class="desc">绑定商户后即可使用核销功能</text>
      </view>
      
      <view class="input-section">
        <view class="input-item">
          <text class="label">绑定码</text>
          <input 
            type="text" 
            v-model="bindCode" 
            placeholder="请输入商户提供的绑定码"
            maxlength="10"
          />
        </view>
      </view>
      
      <view class="tips-section">
        <text class="tips-title">绑定说明:</text>
        <view class="tip-item">
          <text class="dot">•</text>
          <text class="text">请向商户管理员获取绑定码</text>
        </view>
        <view class="tip-item">
          <text class="dot">•</text>
          <text class="text">绑定后可进行券码核销操作</text>
        </view>
        <view class="tip-item">
          <text class="dot">•</text>
          <text class="text">每个账号只能绑定一个商户</text>
        </view>
      </view>
      
      <button class="bind-btn" :disabled="!bindCode" @click="handleBind">
        确认绑定
      </button>
    </view>

    <!-- 扫码绑定入口 -->
    <view class="scan-entry" @click="scanBind">
      <text class="icon">📷</text>
      <text class="text">扫码绑定</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface BindInfo {
  merchantId: string
  merchantName: string
  merchantLogo: string
  role: 'staff' | 'admin'
  bindTime: string
}

const bindInfo = ref<BindInfo | null>(null)
const bindCode = ref('')

const loadBindInfo = async () => {
  try {
    const res = await get<BindInfo | null>('/merchant/employee/bind-info')
    if (res.code === 0) {
      bindInfo.value = res.data
    }
  } catch (e) {
    // 忽略
  }
}

const handleBind = async () => {
  if (!bindCode.value) {
    uni.showToast({ title: '请输入绑定码', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '绑定中...' })
  try {
    const res = await post<BindInfo>('/merchant/employee/bind', {
      code: bindCode.value
    })
    
    if (res.code === 0) {
      bindInfo.value = res.data
      uni.showToast({ title: '绑定成功', icon: 'success' })
    } else {
      uni.showToast({ title: res.message || '绑定失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '绑定失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const handleUnbind = () => {
  uni.showModal({
    title: '提示',
    content: '确定要解除绑定吗？解除后将无法进行核销操作',
    success: async (result) => {
      if (result.confirm) {
        try {
          const res = await post('/merchant/employee/unbind')
          if (res.code === 0) {
            bindInfo.value = null
            uni.showToast({ title: '已解除绑定', icon: 'success' })
          }
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const scanBind = () => {
  uni.scanCode({
    onlyFromCamera: true,
    scanType: ['qrCode'],
    success: async (res) => {
      if (res.result) {
        bindCode.value = res.result
        handleBind()
      }
    }
  })
}

onMounted(() => {
  loadBindInfo()
})
</script>

<style lang="scss">
.bind-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24rpx;
}

.bind-status {
  background: #fff;
  border-radius: 20rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  
  .status-icon {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    font-size: 48rpx;
    color: #fff;
    
    &.success {
      background: #4caf50;
    }
  }
  
  .status-text {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .merchant-card {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 32rpx;
    padding: 24rpx;
    background: #f9f9f9;
    border-radius: 12rpx;
    
    .logo {
      width: 80rpx;
      height: 80rpx;
      border-radius: 12rpx;
    }
    
    .info {
      margin-left: 20rpx;
      text-align: left;
      
      .name {
        display: block;
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .role {
        display: block;
        font-size: 24rpx;
        color: #ff6b35;
        margin-top: 8rpx;
      }
    }
  }
  
  .bind-time {
    margin-top: 24rpx;
    font-size: 24rpx;
    color: #999;
  }
  
  .unbind-btn {
    margin-top: 40rpx;
    padding: 20rpx 60rpx;
    background: #f5f5f5;
    color: #666;
    font-size: 28rpx;
    border-radius: 40rpx;
    border: none;
  }
}

.unbind-status {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
}

.guide-section {
  text-align: center;
  padding-bottom: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .title {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
  }
  
  .desc {
    display: block;
    font-size: 26rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.input-section {
  padding: 32rpx 0;
  
  .input-item {
    .label {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-bottom: 16rpx;
    }
    
    input {
      width: 100%;
      padding: 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 32rpx;
      text-align: center;
      letter-spacing: 4rpx;
    }
  }
}

.tips-section {
  padding: 24rpx;
  background: #fffbf5;
  border-radius: 12rpx;
  
  .tips-title {
    display: block;
    font-size: 26rpx;
    color: #f7931e;
    font-weight: bold;
    margin-bottom: 16rpx;
  }
  
  .tip-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 12rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .dot {
      color: #f7931e;
      margin-right: 8rpx;
    }
    
    .text {
      font-size: 24rpx;
      color: #666;
      flex: 1;
    }
  }
}

.bind-btn {
  margin-top: 40rpx;
  width: 100%;
  padding: 24rpx;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 48rpx;
  border: none;
  
  &[disabled] {
    background: #ccc;
  }
}

.scan-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 32rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 12rpx;
  
  .icon {
    font-size: 36rpx;
    margin-right: 12rpx;
  }
  
  .text {
    font-size: 28rpx;
    color: #666;
  }
}
</style>
