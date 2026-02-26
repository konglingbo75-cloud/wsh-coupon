<template>
  <view class="groupbuy-list">
    <!-- 状态筛选Tab -->
    <view class="status-tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentStatus === null }"
        @tap="switchStatus(null)"
      >全部</view>
      <view 
        class="tab-item" 
        :class="{ active: currentStatus === 0 }"
        @tap="switchStatus(0)"
      >拼团中</view>
      <view 
        class="tab-item" 
        :class="{ active: currentStatus === 1 }"
        @tap="switchStatus(1)"
      >已成团</view>
      <view 
        class="tab-item" 
        :class="{ active: currentStatus === 2 }"
        @tap="switchStatus(2)"
      >已失败</view>
    </view>
    
    <!-- 列表 -->
    <scroll-view 
      class="list-container"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view 
        class="group-item" 
        v-for="item in groups" 
        :key="item.groupOrderId"
        @tap="goDetail(item.groupOrderId)"
      >
        <view class="item-header">
          <view class="merchant-name">{{ item.merchantName }}</view>
          <view class="status-tag" :class="'status-' + item.status">
            {{ item.statusName }}
          </view>
        </view>
        
        <view class="item-body">
          <image class="cover" :src="item.coverImage || '/static/default-activity.png'" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.activityName }}</text>
            <view class="progress">
              <text>{{ item.currentMembers }}/{{ item.requiredMembers }} 人</text>
              <text class="initiator" v-if="item.isInitiator">我发起的</text>
            </view>
            <view class="price-row">
              <text class="price">¥{{ item.groupPrice }}</text>
              <text class="countdown" v-if="item.status === 0 && item.remainingSeconds > 0">
                剩余 {{ formatTime(item.remainingSeconds) }}
              </text>
            </view>
          </view>
        </view>
        
        <view class="item-footer" v-if="item.status === 0">
          <button class="btn-share" open-type="share" :data-item="item" @tap.stop>
            邀请好友
          </button>
          <button class="btn-detail" @tap.stop="goDetail(item.groupOrderId)">
            查看详情
          </button>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="!loading && groups.length === 0">
        <text class="icon">📦</text>
        <text class="text">暂无拼团记录</text>
        <button class="btn-go" @tap="goActivity">去参与拼团</button>
      </view>
      
      <!-- 加载状态 -->
      <view class="loading-tip" v-if="loading">
        <text>加载中...</text>
      </view>
      
      <!-- 没有更多 -->
      <view class="no-more" v-if="!loading && groups.length > 0 && !hasMore">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow, onShareAppMessage } from '@dcloudio/uni-app'
import { getMyGroups, type GroupItem } from '@/api/groupbuy'

const loading = ref(false)
const refreshing = ref(false)
const groups = ref<GroupItem[]>([])
const currentStatus = ref<number | null>(null)
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)

// 格式化时间
function formatTime(seconds: number): string {
  if (seconds <= 0) return '00:00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 加载数据
async function loadData(isRefresh = false) {
  if (loading.value) return
  
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }
  
  loading.value = true
  try {
    const res = await getMyGroups({
      status: currentStatus.value ?? undefined,
      page: page.value,
      pageSize
    })
    
    if (isRefresh) {
      groups.value = res.groups
    } else {
      groups.value.push(...res.groups)
    }
    
    hasMore.value = groups.value.length < res.total
  } catch (err) {
    console.error('加载拼团列表失败', err)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
function onRefresh() {
  refreshing.value = true
  loadData(true)
}

// 加载更多
function loadMore() {
  if (!hasMore.value || loading.value) return
  page.value++
  loadData()
}

// 切换状态
function switchStatus(status: number | null) {
  currentStatus.value = status
  loadData(true)
}

// 跳转详情
function goDetail(groupOrderId: number) {
  uni.navigateTo({ url: `/pages/consumer/groupbuy/detail?id=${groupOrderId}` })
}

// 跳转活动广场
function goActivity() {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

// 分享
onShareAppMessage((options) => {
  const item = (options as any).target?.dataset?.item as GroupItem
  if (item) {
    return {
      title: `${item.activityName} 仅需¥${item.groupPrice}，快来拼团！`,
      path: `/pages/consumer/groupbuy/detail?id=${item.groupOrderId}`
    }
  }
  return {
    title: '快来参与拼团吧',
    path: '/pages/index/index'
  }
})

onShow(() => {
  loadData(true)
})
</script>

<style lang="scss" scoped>
.groupbuy-list {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.status-tabs {
  display: flex;
  background: #fff;
  padding: 0 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 28rpx 0;
    font-size: 28rpx;
    color: #666;
    position: relative;
    
    &.active {
      color: #667eea;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 48rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }
  }
}

.list-container {
  flex: 1;
  padding: 24rpx;
}

.group-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  
  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .merchant-name {
      font-size: 26rpx;
      color: #666;
    }
    
    .status-tag {
      font-size: 24rpx;
      padding: 4rpx 16rpx;
      border-radius: 4rpx;
      
      &.status-0 {
        background: #fff3e0;
        color: #ff9800;
      }
      &.status-1 {
        background: #e8f5e9;
        color: #4caf50;
      }
      &.status-2 {
        background: #ffebee;
        color: #f44336;
      }
      &.status-3 {
        background: #f5f5f5;
        color: #999;
      }
    }
  }
  
  .item-body {
    display: flex;
    padding: 24rpx;
    gap: 20rpx;
    
    .cover {
      width: 160rpx;
      height: 160rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .name {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .progress {
        display: flex;
        align-items: center;
        gap: 16rpx;
        font-size: 24rpx;
        color: #999;
        
        .initiator {
          background: #667eea;
          color: #fff;
          padding: 2rpx 12rpx;
          border-radius: 4rpx;
          font-size: 20rpx;
        }
      }
      
      .price-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .price {
          font-size: 32rpx;
          color: #e53935;
          font-weight: 600;
        }
        
        .countdown {
          font-size: 22rpx;
          color: #ff9800;
          background: #fff3e0;
          padding: 4rpx 12rpx;
          border-radius: 4rpx;
        }
      }
    }
  }
  
  .item-footer {
    display: flex;
    gap: 20rpx;
    padding: 0 24rpx 24rpx;
    
    button {
      flex: 1;
      height: 64rpx;
      font-size: 26rpx;
      border-radius: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.btn-share {
        background: #fff;
        border: 2rpx solid #667eea;
        color: #667eea;
      }
      
      &.btn-detail {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border: none;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  
  .icon {
    font-size: 80rpx;
    margin-bottom: 24rpx;
  }
  
  .text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 32rpx;
  }
  
  .btn-go {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 28rpx;
    padding: 20rpx 48rpx;
    border-radius: 40rpx;
    border: none;
  }
}

.loading-tip {
  text-align: center;
  padding: 32rpx;
  color: #999;
  font-size: 26rpx;
}

.no-more {
  text-align: center;
  padding: 32rpx;
  color: #ccc;
  font-size: 24rpx;
}
</style>
