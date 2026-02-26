<template>
  <view class="invoice-page">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view 
        v-for="item in statusOptions" 
        :key="item.value"
        :class="['filter-item', { active: currentStatus === item.value }]"
        @click="changeStatus(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <!-- 发票列表 -->
    <scroll-view 
      class="invoice-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-wrapper">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <image src="/static/images/empty-invoice.png" mode="aspectFit" class="empty-image" />
        <text class="empty-text">暂无发票记录</text>
      </view>

      <view v-else>
        <view 
          v-for="item in list" 
          :key="item.invoiceId"
          class="invoice-card"
          @click="viewDetail(item)"
        >
          <view class="card-header">
            <view class="merchant-info">
              <text class="merchant-name">{{ item.merchantName }}</text>
              <view :class="['status-tag', `status-${item.invoiceStatus}`]">
                {{ item.invoiceStatusName }}
              </view>
            </view>
            <text class="amount">¥{{ item.invoiceAmount?.toFixed(2) || '0.00' }}</text>
          </view>
          
          <view class="card-body">
            <view class="info-row">
              <text class="label">发票抬头</text>
              <text class="value">{{ item.invoiceTitle || '-' }}</text>
            </view>
            <view class="info-row">
              <text class="label">发票类型</text>
              <text class="value">{{ item.invoiceTypeName || '-' }}</text>
            </view>
            <view v-if="item.invoiceNo" class="info-row">
              <text class="label">发票号码</text>
              <text class="value">{{ item.invoiceNo }}</text>
            </view>
          </view>
          
          <view class="card-footer">
            <text class="time">{{ formatTime(item.createdAt) }}</text>
            <view v-if="item.invoiceStatus === 1 && item.invoiceUrl" class="action-btn" @click.stop="downloadInvoice(item)">
              下载发票
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && list.length > 0" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-if="!hasMore && list.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="btn-settings" @click="goToSettings">
        发票抬头管理
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInvoiceList, type InvoiceItem } from '@/api/invoice'

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '未开票', value: 0 },
  { label: '已开票', value: 1 },
  { label: '开票中', value: 2 },
]

const currentStatus = ref<number | undefined>(undefined)
const list = ref<InvoiceItem[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

const fetchList = async (reset = false) => {
  if (loading.value) return
  if (reset) {
    page.value = 1
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getInvoiceList({
      status: currentStatus.value,
      page: page.value,
      size: 10
    })
    const data = res.data || { list: [], total: 0 }
    if (reset) {
      list.value = data.list || []
    } else {
      list.value.push(...(data.list || []))
    }
    hasMore.value = list.value.length < data.total
    page.value++
  } catch (e) {
    console.error('获取发票列表失败', e)
  } finally {
    loading.value = false
  }
}

const changeStatus = (status: number | undefined) => {
  currentStatus.value = status
  fetchList(true)
}

const loadMore = () => {
  if (hasMore.value && !loading.value) {
    fetchList()
  }
}

const viewDetail = (item: InvoiceItem) => {
  uni.navigateTo({
    url: `/pages/consumer/invoice/detail?id=${item.invoiceId}`
  })
}

const downloadInvoice = (item: InvoiceItem) => {
  if (item.invoiceUrl) {
    uni.downloadFile({
      url: item.invoiceUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true
          })
        }
      },
      fail: () => {
        uni.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  }
}

const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/consumer/invoice/settings'
  })
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.slice(0, 10)
}

onMounted(() => {
  fetchList(true)
})
</script>

<style lang="scss" scoped>
.invoice-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f5f7fa 100%);
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  padding: 24rpx 32rpx;
  background: #fff;
  gap: 20rpx;
}

.filter-item {
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  color: #666;
  background: #f5f7fa;
  border-radius: 32rpx;
  transition: all 0.3s;
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.4);
  }
}

.invoice-list {
  flex: 1;
  padding: 24rpx 32rpx;
  padding-bottom: 140rpx;
}

.invoice-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.merchant-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.status-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  
  &.status-0 {
    background: #fff3e0;
    color: #f7931e;
  }
  &.status-1 {
    background: #e8f5e9;
    color: #4caf50;
  }
  &.status-2 {
    background: #e3f2fd;
    color: #2196f3;
  }
  &.status-3 {
    background: #ffebee;
    color: #f44336;
  }
}

.amount {
  font-size: 36rpx;
  font-weight: 700;
  color: #ff6b35;
}

.card-body {
  border-top: 1rpx dashed #eee;
  padding-top: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
  
  .label {
    font-size: 26rpx;
    color: #999;
  }
  
  .value {
    font-size: 26rpx;
    color: #333;
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.action-btn {
  font-size: 24rpx;
  color: #667eea;
  padding: 10rpx 24rpx;
  border: 1rpx solid #667eea;
  border-radius: 24rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.btn-settings {
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 48rpx;
}

.loading-wrapper, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-image {
  width: 240rpx;
  height: 240rpx;
  margin-bottom: 24rpx;
}

.empty-text, .loading-text {
  font-size: 28rpx;
  color: #999;
}

.loading-more, .no-more {
  text-align: center;
  padding: 24rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
