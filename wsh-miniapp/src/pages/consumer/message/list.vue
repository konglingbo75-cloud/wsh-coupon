<template>
  <view class="message-list-page">
    <!-- 消息分类 -->
    <view class="category-bar">
      <view 
        v-for="cat in categories" 
        :key="cat.key" 
        class="category-item" 
        :class="{ active: currentCategory === cat.key }"
        @click="currentCategory = cat.key"
      >
        <text class="icon">{{ cat.icon }}</text>
        <text class="label">{{ cat.label }}</text>
        <view v-if="cat.unread > 0" class="badge">{{ cat.unread > 99 ? '99+' : cat.unread }}</view>
      </view>
    </view>

    <!-- 消息列表 -->
    <view class="message-list">
      <view 
        v-for="item in messages" 
        :key="item.id" 
        class="message-item"
        :class="{ unread: !item.isRead }"
        @click="handleMessage(item)"
      >
        <view class="icon-wrap" :class="item.type">
          <text>{{ getTypeIcon(item.type) }}</text>
        </view>
        <view class="content">
          <view class="header">
            <text class="title">{{ item.title }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
          <text class="summary">{{ item.summary }}</text>
        </view>
        <view v-if="!item.isRead" class="dot"></view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && messages.length === 0" class="empty-state">
        <image src="/static/empty-message.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无消息</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="messages.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @click="loadMore">加载更多</text>
      </view>
    </view>

    <!-- 全部已读按钮 -->
    <view v-if="hasUnread" class="mark-all-read" @click="markAllRead">
      <text>全部已读</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface MessageItem {
  id: string
  type: 'equity' | 'order' | 'activity' | 'system'
  title: string
  summary: string
  time: string
  isRead: boolean
  targetId?: string
  targetType?: string
}

const categories = ref([
  { key: 'all', label: '全部', icon: '📬', unread: 0 },
  { key: 'equity', label: '权益', icon: '🎁', unread: 0 },
  { key: 'order', label: '订单', icon: '📋', unread: 0 },
  { key: 'activity', label: '活动', icon: '🎉', unread: 0 },
  { key: 'system', label: '系统', icon: '🔔', unread: 0 }
])

const loading = ref(false)
const currentCategory = ref('all')
const messages = ref<MessageItem[]>([])
const page = ref(1)
const pageSize = 20
const noMore = ref(false)

const hasUnread = computed(() => {
  return messages.value.some(m => !m.isRead)
})

const getTypeIcon = (type: string) => {
  const map: Record<string, string> = {
    equity: '🎁',
    order: '📋',
    activity: '🎉',
    system: '🔔'
  }
  return map[type] || '📬'
}

const loadMessages = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    noMore.value = false
    messages.value = []
  }
  
  loading.value = true
  try {
    const res = await get<{
      list: MessageItem[]
      categoryCounts: Record<string, number>
    }>('/consumer/messages', {
      category: currentCategory.value === 'all' ? undefined : currentCategory.value,
      page: page.value,
      pageSize
    })
    
    if (res.code === 0) {
      const list = res.data.list || []
      if (reset) {
        messages.value = list
      } else {
        messages.value = [...messages.value, ...list]
      }
      
      if (list.length < pageSize) {
        noMore.value = true
      }
      
      // 更新各分类未读数
      if (res.data.categoryCounts) {
        categories.value = categories.value.map(cat => ({
          ...cat,
          unread: res.data.categoryCounts[cat.key] || 0
        }))
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (noMore.value || loading.value) return
  page.value++
  loadMessages()
}

const handleMessage = async (item: MessageItem) => {
  // 标记为已读
  if (!item.isRead) {
    try {
      await post(`/consumer/messages/${item.id}/read`)
      item.isRead = true
      // 更新未读数
      const cat = categories.value.find(c => c.key === item.type)
      if (cat && cat.unread > 0) {
        cat.unread--
      }
      const allCat = categories.value.find(c => c.key === 'all')
      if (allCat && allCat.unread > 0) {
        allCat.unread--
      }
    } catch (e) {
      // 忽略
    }
  }
  
  // 跳转到对应详情页
  if (item.targetType && item.targetId) {
    switch (item.targetType) {
      case 'order':
        uni.navigateTo({ url: `/pages/consumer/order/detail?id=${item.targetId}` })
        break
      case 'voucher':
        uni.navigateTo({ url: `/pages/consumer/voucher/detail?id=${item.targetId}` })
        break
      case 'activity':
        uni.navigateTo({ url: `/pages/consumer/activity/detail?id=${item.targetId}` })
        break
      case 'equity':
        uni.navigateTo({ url: `/pages/consumer/equity/summary` })
        break
    }
  }
}

const markAllRead = async () => {
  uni.showModal({
    title: '提示',
    content: '确定将所有消息标记为已读吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post('/consumer/messages/read-all', {
            category: currentCategory.value === 'all' ? undefined : currentCategory.value
          })
          
          // 更新本地状态
          messages.value = messages.value.map(m => ({ ...m, isRead: true }))
          
          // 更新未读数
          if (currentCategory.value === 'all') {
            categories.value = categories.value.map(cat => ({ ...cat, unread: 0 }))
          } else {
            const cat = categories.value.find(c => c.key === currentCategory.value)
            if (cat) {
              cat.unread = 0
            }
          }
          
          uni.showToast({ title: '已全部标为已读', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

watch(currentCategory, () => {
  loadMessages(true)
})

onMounted(() => {
  loadMessages(true)
})
</script>

<style lang="scss">
.message-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 100rpx;
}

.category-bar {
  display: flex;
  background: #fff;
  padding: 16rpx 0;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .category-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx 0;
    position: relative;
    
    &.active {
      .icon {
        transform: scale(1.2);
      }
      
      .label {
        color: #ff6b35;
        font-weight: bold;
      }
    }
    
    .icon {
      font-size: 36rpx;
      transition: transform 0.2s;
    }
    
    .label {
      font-size: 24rpx;
      color: #666;
      margin-top: 8rpx;
    }
    
    .badge {
      position: absolute;
      top: 8rpx;
      right: 16rpx;
      min-width: 32rpx;
      height: 32rpx;
      background: #ff4757;
      color: #fff;
      font-size: 20rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 8rpx;
    }
  }
}

.message-list {
  padding: 24rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  position: relative;
  
  &.unread {
    background: #fffbf5;
  }
  
  .icon-wrap {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    flex-shrink: 0;
    
    &.equity { background: #fff3e0; }
    &.order { background: #e3f2fd; }
    &.activity { background: #fce4ec; }
    &.system { background: #f5f5f5; }
  }
  
  .content {
    flex: 1;
    margin-left: 20rpx;
    min-width: 0;
    
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .time {
        font-size: 24rpx;
        color: #999;
        margin-left: 16rpx;
        flex-shrink: 0;
      }
    }
    
    .summary {
      display: block;
      margin-top: 12rpx;
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
  
  .dot {
    position: absolute;
    top: 28rpx;
    right: 24rpx;
    width: 16rpx;
    height: 16rpx;
    background: #ff4757;
    border-radius: 50%;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  
  .empty-icon {
    width: 200rpx;
    height: 200rpx;
    opacity: 0.6;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 24rpx;
  }
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
}

.mark-all-read {
  position: fixed;
  bottom: 40rpx;
  left: 50%;
  transform: translateX(-50%);
  padding: 16rpx 40rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  
  text {
    font-size: 28rpx;
    color: #ff6b35;
  }
}
</style>
