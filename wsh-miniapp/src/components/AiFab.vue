<template>
  <view class="ai-fab" @tap="goAiChat">
    <view class="fab-inner">
      <text class="fab-icon">AI</text>
    </view>
    <view class="fab-ring"></view>
    <view class="fab-ring-outer"></view>
  </view>
</template>

<script setup lang="ts">
function goAiChat() {
  uni.navigateTo({ url: '/pages/consumer/ai/chat' })
}
</script>

<style lang="scss" scoped>
/* 赛博朋克配色 */
$cyber-primary: #00f2ff;
$cyber-purple: #7928ca;

.ai-fab {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx;
  width: 100rpx;
  height: 100rpx;
  z-index: 999;
  animation: float 3s ease-in-out infinite;
  
  .fab-inner {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, $cyber-primary 0%, $cyber-purple 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 
      0 0 20rpx rgba($cyber-primary, 0.5),
      0 0 40rpx rgba($cyber-primary, 0.3),
      0 0 60rpx rgba($cyber-primary, 0.1);
    
    .fab-icon {
      color: #fff;
      font-size: 28rpx;
      font-weight: 700;
      letter-spacing: 2rpx;
      text-shadow: 0 0 10rpx rgba(255, 255, 255, 0.5);
    }
  }
  
  .fab-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    border: 2rpx solid rgba($cyber-primary, 0.5);
    animation: pulse-ring 2s ease-in-out infinite;
  }
  
  .fab-ring-outer {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 2rpx dashed rgba($cyber-primary, 0.3);
    animation: rotate-ring 10s linear infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

@keyframes pulse-ring {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.6;
  }
}

@keyframes rotate-ring {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}
</style>
