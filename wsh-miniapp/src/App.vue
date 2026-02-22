<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

onLaunch(() => {
  console.log('App Launch')
  // 检查登录状态
  const userStore = useUserStore()
  userStore.checkLoginStatus()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style>
/* 全局样式 */
page {
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 通用卡片样式 */
.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 按钮样式 */
.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border: none;
  border-radius: 44rpx;
  padding: 24rpx 48rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.btn-primary:active {
  opacity: 0.9;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 48rpx;
}

.empty-state image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}

.empty-state text {
  color: #999999;
  font-size: 28rpx;
}
</style>
