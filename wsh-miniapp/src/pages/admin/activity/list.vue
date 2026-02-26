<template>
  <view class="activity-list-page">
    <!-- 顶部操作栏 -->
    <view class="action-bar">
      <view class="search-wrap">
        <input type="text" v-model="searchKey" placeholder="搜索活动名称" @confirm="handleSearch" />
      </view>
      <button class="add-btn" @click="goCreateActivity">+ 新建</button>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view 
        v-for="tab in statusTabs" 
        :key="tab.key" 
        class="filter-item" 
        :class="{ active: currentStatus === tab.key }"
        @click="currentStatus = tab.key"
      >
        <text>{{ tab.label }}</text>
        <text v-if="tab.count > 0" class="count">({{ tab.count }})</text>
      </view>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list">
      <view v-for="item in activities" :key="item.id" class="activity-card">
        <view class="card-header">
          <image class="cover" :src="item.coverUrl" mode="aspectFill" />
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <view class="tags">
              <text class="tag type">{{ getTypeName(item.type) }}</text>
              <text class="tag status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
            </view>
            <view class="price-row">
              <text class="price">¥{{ item.price }}</text>
              <text v-if="item.originalPrice" class="original">¥{{ item.originalPrice }}</text>
            </view>
          </view>
        </view>
        
        <view class="card-body">
          <view class="stat-row">
            <view class="stat-item">
              <text class="num">{{ item.soldCount }}</text>
              <text class="label">已售</text>
            </view>
            <view class="stat-item">
              <text class="num">{{ item.remainStock }}</text>
              <text class="label">库存</text>
            </view>
            <view class="stat-item">
              <text class="num">{{ item.verifiedCount }}</text>
              <text class="label">已核销</text>
            </view>
            <view class="stat-item">
              <text class="num">¥{{ item.revenue }}</text>
              <text class="label">收入</text>
            </view>
          </view>
          <view class="date-row">
            <text>有效期: {{ item.validStart }} 至 {{ item.validEnd }}</text>
          </view>
        </view>
        
        <view class="card-footer">
          <button class="action-btn" @click="editActivity(item)">编辑</button>
          <button v-if="item.status === 0" class="action-btn primary" @click="publishActivity(item)">发布</button>
          <button v-if="item.status === 1" class="action-btn warning" @click="pauseActivity(item)">暂停</button>
          <button v-if="item.status === 2" class="action-btn primary" @click="resumeActivity(item)">恢复</button>
          <button class="action-btn" @click="viewDetail(item)">数据</button>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && activities.length === 0" class="empty-state">
        <image src="/static/empty-activity.png" mode="aspectFit" class="empty-icon" />
        <text class="empty-text">暂无活动</text>
        <button class="create-btn" @click="goCreateActivity">创建活动</button>
      </view>

      <!-- 加载更多 -->
      <view v-if="activities.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get, post } from '@/api/request'

interface ActivityItem {
  id: number
  title: string
  type: number
  coverUrl: string
  price: number
  originalPrice?: number
  status: number
  soldCount: number
  remainStock: number
  verifiedCount: number
  revenue: number
  validStart: string
  validEnd: string
}

const statusTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'active', label: '进行中', count: 0 },
  { key: 'draft', label: '草稿', count: 0 },
  { key: 'paused', label: '已暂停', count: 0 },
  { key: 'ended', label: '已结束', count: 0 }
])

const searchKey = ref('')
const currentStatus = ref('all')
const activities = ref<ActivityItem[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

const getTypeName = (type: number) => {
  const map: Record<number, string> = {
    1: '代金券',
    2: '储值',
    3: '积分',
    4: '团购'
  }
  return map[type] || '未知'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '草稿',
    1: '进行中',
    2: '已暂停',
    3: '已结束'
  }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'draft',
    1: 'active',
    2: 'paused',
    3: 'ended'
  }
  return map[status] || ''
}

// 状态 key → 数字映射
const statusKeyToNum: Record<string, number | undefined> = {
  all: undefined,
  draft: 0,
  active: 1,
  paused: 2,
  ended: 3
}

const loadActivities = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    noMore.value = false
    activities.value = []
  }
  
  loading.value = true
  try {
    const res = await get<{
      list: ActivityItem[]
      statusCounts: Record<string, number>
    }>('/v1/merchant/activities', {
      status: statusKeyToNum[currentStatus.value],
      keyword: searchKey.value || undefined,
      page: page.value,
      pageSize
    })
    
    const list = res.list || []
    if (reset) {
      activities.value = list
    } else {
      activities.value = [...activities.value, ...list]
    }
    
    if (list.length < pageSize) {
      noMore.value = true
    }
    
    // 更新状态计数
    if (res.statusCounts) {
      statusTabs.value = statusTabs.value.map(tab => ({
        ...tab,
        count: res.statusCounts[tab.key] || 0
      }))
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadActivities(true)
}

const goCreateActivity = () => {
  uni.navigateTo({ url: '/pages/admin/activity/edit' })
}

const editActivity = (item: ActivityItem) => {
  uni.navigateTo({ url: `/pages/admin/activity/edit?id=${item.id}` })
}

const viewDetail = (item: ActivityItem) => {
  uni.navigateTo({ url: `/pages/admin/activity/detail?id=${item.id}` })
}

const publishActivity = async (item: ActivityItem) => {
  uni.showModal({
    title: '确认发布',
    content: '发布后活动将对用户可见，确定发布吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/v1/merchant/activities/${item.id}/publish`)
          item.status = 1
          uni.showToast({ title: '已发布', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '发布失败', icon: 'none' })
        }
      }
    }
  })
}

const pauseActivity = async (item: ActivityItem) => {
  uni.showModal({
    title: '确认暂停',
    content: '暂停后活动将不再对用户展示，确定暂停吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/v1/merchant/activities/${item.id}/pause`)
          item.status = 2
          uni.showToast({ title: '已暂停', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const resumeActivity = async (item: ActivityItem) => {
  try {
    await post(`/v1/merchant/activities/${item.id}/resume`)
    item.status = 1
    uni.showToast({ title: '已恢复', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

watch(currentStatus, () => {
  loadActivities(true)
})

onMounted(() => {
  loadActivities(true)
})
</script>

<style lang="scss">
.activity-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.action-bar {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx 24rpx;
  
  .search-wrap {
    flex: 1;
    
    input {
      padding: 16rpx 24rpx;
      background: #f5f5f5;
      border-radius: 32rpx;
      font-size: 28rpx;
    }
  }
  
  .add-btn {
    margin-left: 16rpx;
    padding: 16rpx 32rpx;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: #fff;
    font-size: 28rpx;
    border-radius: 32rpx;
    border: none;
  }
}

.filter-bar {
  display: flex;
  background: #fff;
  padding: 0 12rpx;
  border-top: 1rpx solid #f0f0f0;
  overflow-x: auto;
  white-space: nowrap;
  
  .filter-item {
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #666;
    position: relative;
    flex-shrink: 0;
    
    &.active {
      color: #ff6b35;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: #ff6b35;
        border-radius: 2rpx;
      }
    }
    
    .count {
      font-size: 24rpx;
    }
  }
}

.activity-list {
  padding: 24rpx;
}

.activity-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .card-header {
    display: flex;
    padding: 20rpx;
    
    .cover {
      width: 160rpx;
      height: 160rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }
    
    .info {
      flex: 1;
      margin-left: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .title {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .tags {
        display: flex;
        margin-top: 8rpx;
        
        .tag {
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 22rpx;
          margin-right: 12rpx;
          
          &.type {
            background: #f5f5f5;
            color: #666;
          }
          
          &.status {
            &.draft { background: #f5f5f5; color: #999; }
            &.active { background: #e8f5e9; color: #4caf50; }
            &.paused { background: #fff3e0; color: #ff9800; }
            &.ended { background: #f5f5f5; color: #999; }
          }
        }
      }
      
      .price-row {
        .price {
          font-size: 32rpx;
          font-weight: bold;
          color: #ff6b35;
        }
        
        .original {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 12rpx;
        }
      }
    }
  }
  
  .card-body {
    padding: 0 20rpx 20rpx;
    
    .stat-row {
      display: flex;
      padding: 16rpx;
      background: #fafafa;
      border-radius: 12rpx;
      
      .stat-item {
        flex: 1;
        text-align: center;
        
        .num {
          display: block;
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
        }
        
        .label {
          display: block;
          font-size: 22rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }
    }
    
    .date-row {
      margin-top: 12rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    padding: 16rpx 20rpx;
    border-top: 1rpx solid #f5f5f5;
    
    .action-btn {
      margin-left: 16rpx;
      padding: 12rpx 24rpx;
      font-size: 26rpx;
      border-radius: 24rpx;
      background: #f5f5f5;
      color: #666;
      border: none;
      
      &.primary {
        background: linear-gradient(135deg, #ff6b35, #f7931e);
        color: #fff;
      }
      
      &.warning {
        background: #fff3e0;
        color: #ff9800;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  
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
  
  .create-btn {
    margin-top: 32rpx;
    padding: 16rpx 48rpx;
    background: #ff6b35;
    color: #fff;
    font-size: 28rpx;
    border-radius: 32rpx;
    border: none;
  }
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
