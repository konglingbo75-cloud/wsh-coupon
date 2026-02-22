<template>
  <view class="login-page">
    <!-- Logo区域 -->
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">微生活券吧</text>
      <text class="slogan">用好已有的资产权益</text>
    </view>

    <!-- 登录区域 -->
    <view class="login-section">
      <button class="login-btn" @tap="handleLogin" :loading="loading">
        <text class="btn-icon">📱</text>
        <text>微信一键登录</text>
      </button>
      
      <view class="tips">
        <text>登录即表示同意</text>
        <text class="link" @tap="showPrivacy">《用户协议》</text>
        <text>和</text>
        <text class="link" @tap="showPrivacy">《隐私政策》</text>
      </view>
    </view>

    <!-- 底部装饰 -->
    <view class="bottom-decoration">
      <view class="feature-list">
        <view class="feature-item">
          <text class="icon">💰</text>
          <text>权益聚合</text>
        </view>
        <view class="feature-item">
          <text class="icon">⏰</text>
          <text>过期提醒</text>
        </view>
        <view class="feature-item">
          <text class="icon">🎁</text>
          <text>专属优惠</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

async function handleLogin() {
  if (loading.value) return
  
  loading.value = true
  try {
    const result = await userStore.login()
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
    
    // 跳转首页
    setTimeout(() => {
      uni.switchTab({ url: '/pages/tabbar/home/index' })
    }, 500)
    
  } catch (err: any) {
    uni.showToast({
      title: err.message || '登录失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

function showPrivacy() {
  uni.showModal({
    title: '提示',
    content: '用户协议和隐私政策将在正式发布时完善',
    showCancel: false
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

.logo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120rpx;
  
  .logo {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 32rpx;
  }
  
  .app-name {
    font-size: 48rpx;
    font-weight: 700;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .slogan {
    font-size: 28rpx;
    color: #999;
  }
}

.login-section {
  padding: 48rpx;
  
  .login-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 32rpx;
    font-weight: 500;
    border-radius: 48rpx;
    border: none;
    
    .btn-icon {
      font-size: 36rpx;
    }
  }
  
  .tips {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    margin-top: 32rpx;
    font-size: 24rpx;
    color: #999;
    
    .link {
      color: #667eea;
    }
  }
}

.bottom-decoration {
  padding: 48rpx 48rpx 100rpx;
  
  .feature-list {
    display: flex;
    justify-content: space-around;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      
      .icon {
        font-size: 48rpx;
      }
      
      text:last-child {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}
</style>
