<template>
  <view class="scan-page">
    <!-- 扫码区域 -->
    <view class="scan-area">
      <camera 
        v-if="cameraReady"
        class="camera"
        device-position="back"
        flash="auto"
        @error="onCameraError"
      >
        <cover-view class="scan-frame">
          <cover-view class="frame-corner tl"></cover-view>
          <cover-view class="frame-corner tr"></cover-view>
          <cover-view class="frame-corner bl"></cover-view>
          <cover-view class="frame-corner br"></cover-view>
          <cover-view class="scan-line" :style="{ top: scanLineTop + 'px' }"></cover-view>
        </cover-view>
        <cover-view class="scan-tip">
          <cover-view class="tip-text">将券码放入框内，自动扫描</cover-view>
        </cover-view>
      </camera>
      
      <!-- 相机未就绪时的占位 -->
      <view v-else class="camera-placeholder">
        <text class="placeholder-text">正在启动相机...</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <view class="action-item" @click="switchFlash">
        <text class="icon">{{ flashOn ? '🔦' : '💡' }}</text>
        <text class="label">{{ flashOn ? '关闭闪光' : '打开闪光' }}</text>
      </view>
      <view class="action-item main" @click="startScan">
        <text class="icon">📷</text>
        <text class="label">扫一扫</text>
      </view>
      <view class="action-item" @click="inputCode">
        <text class="icon">⌨️</text>
        <text class="label">手动输入</text>
      </view>
    </view>

    <!-- 最近核销记录 -->
    <view class="recent-section">
      <view class="section-header">
        <text class="title">最近核销</text>
        <view class="more" @click="goRecordList">
          <text>全部记录</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="record-list">
        <view v-for="item in recentRecords" :key="item.id" class="record-item">
          <view class="info">
            <text class="name">{{ item.voucherName }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
          <view class="right">
            <text class="amount">¥{{ item.amount }}</text>
            <text class="status" :class="item.status">{{ item.status === 'success' ? '成功' : '失败' }}</text>
          </view>
        </view>
        <view v-if="recentRecords.length === 0" class="empty-record">
          <text>暂无核销记录</text>
        </view>
      </view>
    </view>

    <!-- 手动输入弹窗 -->
    <view v-if="showInputModal" class="input-modal" @click="showInputModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="title">手动输入券码</text>
          <text class="close" @click="showInputModal = false">✕</text>
        </view>
        <view class="modal-body">
          <input 
            type="text" 
            v-model="inputCodeValue" 
            placeholder="请输入券码"
            maxlength="20"
            focus
          />
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="showInputModal = false">取消</button>
          <button class="confirm-btn" @click="confirmInputCode">确认核销</button>
        </view>
      </view>
    </view>

    <!-- 核销结果弹窗 -->
    <view v-if="showResultModal" class="result-modal" @click="closeResultModal">
      <view class="modal-content" :class="verifyResult?.success ? 'success' : 'fail'" @click.stop>
        <view class="result-icon">
          <text>{{ verifyResult?.success ? '✓' : '✕' }}</text>
        </view>
        <text class="result-text">{{ verifyResult?.success ? '核销成功' : '核销失败' }}</text>
        <text class="result-message">{{ verifyResult?.message }}</text>
        
        <view v-if="verifyResult?.success && verifyResult?.voucherInfo" class="voucher-info">
          <view class="info-row">
            <text class="label">券名称</text>
            <text class="value">{{ verifyResult.voucherInfo.name }}</text>
          </view>
          <view class="info-row">
            <text class="label">抵扣金额</text>
            <text class="value">¥{{ verifyResult.voucherInfo.amount }}</text>
          </view>
          <view class="info-row">
            <text class="label">用户手机</text>
            <text class="value">{{ verifyResult.voucherInfo.phone }}</text>
          </view>
        </view>
        
        <button class="close-btn" @click="closeResultModal">
          {{ verifyResult?.success ? '继续核销' : '重新扫码' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { post, get } from '@/api/request'

interface RecordItem {
  id: string
  voucherName: string
  amount: number
  time: string
  status: 'success' | 'fail'
}

interface VerifyResult {
  success: boolean
  message: string
  voucherInfo?: {
    name: string
    amount: number
    phone: string
  }
}

const cameraReady = ref(false)
const flashOn = ref(false)
const scanLineTop = ref(50)
const recentRecords = ref<RecordItem[]>([])
const showInputModal = ref(false)
const inputCodeValue = ref('')
const showResultModal = ref(false)
const verifyResult = ref<VerifyResult | null>(null)

let scanLineTimer: any = null

const initCamera = () => {
  // 检查相机权限
  uni.authorize({
    scope: 'scope.camera',
    success: () => {
      cameraReady.value = true
      startScanLineAnimation()
    },
    fail: () => {
      uni.showModal({
        title: '提示',
        content: '需要相机权限才能扫码核销',
        confirmText: '去设置',
        success: (res) => {
          if (res.confirm) {
            uni.openSetting()
          }
        }
      })
    }
  })
}

const startScanLineAnimation = () => {
  let direction = 1
  scanLineTimer = setInterval(() => {
    scanLineTop.value += direction * 2
    if (scanLineTop.value >= 200) {
      direction = -1
    } else if (scanLineTop.value <= 50) {
      direction = 1
    }
  }, 20)
}

const onCameraError = (e: any) => {
  console.error('Camera error:', e)
  uni.showToast({ title: '相机启动失败', icon: 'none' })
}

const switchFlash = () => {
  flashOn.value = !flashOn.value
}

const startScan = () => {
  uni.scanCode({
    onlyFromCamera: true,
    scanType: ['qrCode', 'barCode'],
    success: (res) => {
      if (res.result) {
        verifyCode(res.result)
      }
    },
    fail: () => {
      uni.showToast({ title: '扫码取消', icon: 'none' })
    }
  })
}

const inputCode = () => {
  inputCodeValue.value = ''
  showInputModal.value = true
}

const confirmInputCode = () => {
  if (!inputCodeValue.value.trim()) {
    uni.showToast({ title: '请输入券码', icon: 'none' })
    return
  }
  showInputModal.value = false
  verifyCode(inputCodeValue.value.trim())
}

const verifyCode = async (code: string) => {
  uni.showLoading({ title: '核销中...' })
  
  try {
    const res = await post<VerifyResult>('/merchant/verification/verify', { code })
    
    verifyResult.value = {
      success: res.code === 0,
      message: res.message || (res.code === 0 ? '核销成功' : '核销失败'),
      voucherInfo: res.data?.voucherInfo
    }
    
    showResultModal.value = true
    
    // 刷新最近记录
    if (res.code === 0) {
      loadRecentRecords()
    }
  } catch (e) {
    verifyResult.value = {
      success: false,
      message: '网络错误，请重试'
    }
    showResultModal.value = true
  } finally {
    uni.hideLoading()
  }
}

const closeResultModal = () => {
  showResultModal.value = false
  verifyResult.value = null
}

const loadRecentRecords = async () => {
  try {
    const res = await get<{ list: RecordItem[] }>('/merchant/verification/recent', { limit: 5 })
    if (res.code === 0) {
      recentRecords.value = res.data.list || []
    }
  } catch (e) {
    // 忽略
  }
}

const goRecordList = () => {
  uni.navigateTo({ url: '/pages/merchant/verification/record' })
}

onMounted(() => {
  initCamera()
  loadRecentRecords()
})

onUnmounted(() => {
  if (scanLineTimer) {
    clearInterval(scanLineTimer)
  }
})
</script>

<style lang="scss">
.scan-page {
  min-height: 100vh;
  background-color: #000;
}

.scan-area {
  height: 500rpx;
  position: relative;
  
  .camera {
    width: 100%;
    height: 100%;
  }
  
  .camera-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #1a1a1a;
    
    .placeholder-text {
      color: #999;
      font-size: 28rpx;
    }
  }
  
  .scan-frame {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 400rpx;
    height: 400rpx;
    
    .frame-corner {
      position: absolute;
      width: 40rpx;
      height: 40rpx;
      border-color: #ff6b35;
      border-style: solid;
      
      &.tl { top: 0; left: 0; border-width: 4rpx 0 0 4rpx; }
      &.tr { top: 0; right: 0; border-width: 4rpx 4rpx 0 0; }
      &.bl { bottom: 0; left: 0; border-width: 0 0 4rpx 4rpx; }
      &.br { bottom: 0; right: 0; border-width: 0 4rpx 4rpx 0; }
    }
    
    .scan-line {
      position: absolute;
      left: 0;
      width: 100%;
      height: 4rpx;
      background: linear-gradient(to right, transparent, #ff6b35, transparent);
    }
  }
  
  .scan-tip {
    position: absolute;
    bottom: 40rpx;
    left: 0;
    right: 0;
    
    .tip-text {
      text-align: center;
      color: #fff;
      font-size: 26rpx;
    }
  }
}

.action-bar {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 24rpx;
  background: #1a1a1a;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .icon {
      width: 80rpx;
      height: 80rpx;
      background: #333;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
    }
    
    &.main .icon {
      width: 120rpx;
      height: 120rpx;
      background: linear-gradient(135deg, #ff6b35, #f7931e);
      font-size: 48rpx;
    }
    
    .label {
      margin-top: 12rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
}

.recent-section {
  background: #fff;
  margin: 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
  
  .more {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #999;
    
    .arrow {
      margin-left: 8rpx;
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
      .name {
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
        
        &.success { color: #4caf50; }
        &.fail { color: #f44336; }
      }
    }
  }
  
  .empty-record {
    text-align: center;
    padding: 40rpx;
    color: #999;
    font-size: 26rpx;
  }
}

.input-modal, .result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.input-modal .modal-content {
  width: 80%;
  background: #fff;
  border-radius: 20rpx;
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
    }
    
    .close {
      font-size: 36rpx;
      color: #999;
    }
  }
  
  .modal-body {
    padding: 32rpx 24rpx;
    
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
  
  .modal-footer {
    display: flex;
    border-top: 1rpx solid #f0f0f0;
    
    button {
      flex: 1;
      padding: 24rpx;
      font-size: 30rpx;
      border: none;
      border-radius: 0;
      
      &.cancel-btn {
        background: #fff;
        color: #666;
      }
      
      &.confirm-btn {
        background: #ff6b35;
        color: #fff;
      }
    }
  }
}

.result-modal .modal-content {
  width: 80%;
  background: #fff;
  border-radius: 20rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  
  .result-icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    font-size: 60rpx;
    color: #fff;
  }
  
  &.success .result-icon {
    background: #4caf50;
  }
  
  &.fail .result-icon {
    background: #f44336;
  }
  
  .result-text {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .result-message {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-top: 12rpx;
  }
  
  .voucher-info {
    margin-top: 32rpx;
    padding: 24rpx;
    background: #f9f9f9;
    border-radius: 12rpx;
    text-align: left;
    
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 12rpx 0;
      
      .label {
        font-size: 26rpx;
        color: #999;
      }
      
      .value {
        font-size: 26rpx;
        color: #333;
      }
    }
  }
  
  .close-btn {
    margin-top: 32rpx;
    padding: 20rpx 60rpx;
    background: #ff6b35;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
    border: none;
  }
}
</style>
