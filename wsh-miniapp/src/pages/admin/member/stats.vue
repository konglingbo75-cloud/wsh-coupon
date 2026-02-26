<template>
  <view class="member-stats-page">
    <!-- 数据概览 -->
    <view class="stats-card">
      <view class="stats-header">
        <text class="title">会员数据</text>
        <picker mode="date" :value="selectedDate" @change="onDateChange" fields="month">
          <view class="date-picker">
            <text>{{ selectedDate }}</text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>
      <view class="stats-body">
        <view class="stat-item main">
          <text class="num">{{ stats.totalMembers }}</text>
          <text class="label">累计会员</text>
        </view>
        <view class="stat-row">
          <view class="stat-item">
            <text class="num">{{ stats.newMembers }}</text>
            <text class="label">本月新增</text>
          </view>
          <view class="stat-item">
            <text class="num">{{ stats.activeMembers }}</text>
            <text class="label">活跃会员</text>
          </view>
          <view class="stat-item">
            <text class="num">{{ stats.repeatRate }}%</text>
            <text class="label">复购率</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 会员等级分布 -->
    <view class="section-card">
      <view class="section-header">
        <text class="title">等级分布</text>
      </view>
      <view class="level-chart">
        <view v-for="item in levelDistribution" :key="item.level" class="level-bar">
          <text class="level-name">VIP{{ item.level }}</text>
          <view class="bar-wrap">
            <view class="bar" :style="{ width: item.percent + '%' }"></view>
          </view>
          <text class="count">{{ item.count }}人</text>
        </view>
      </view>
    </view>

    <!-- 消费排行 -->
    <view class="section-card">
      <view class="section-header">
        <text class="title">消费排行</text>
        <view class="tabs">
          <text :class="{ active: rankType === 'amount' }" @click="rankType = 'amount'">金额</text>
          <text :class="{ active: rankType === 'count' }" @click="rankType = 'count'">次数</text>
        </view>
      </view>
      <view class="rank-list">
        <view v-for="(item, index) in topMembers" :key="item.id" class="rank-item">
          <view class="rank-badge" :class="{ top: index < 3 }">{{ index + 1 }}</view>
          <image class="avatar" :src="item.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.nickname || item.phone }}</text>
            <text class="join-time">加入: {{ item.joinDate }}</text>
          </view>
          <view class="value">
            <text class="num">{{ rankType === 'amount' ? '¥' + item.totalAmount : item.orderCount + '次' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 会员列表入口 -->
    <view class="member-list-entry" @click="goMemberList">
      <text class="text">查看全部会员</text>
      <text class="arrow">›</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { get } from '@/api/request'

interface Stats {
  totalMembers: number
  newMembers: number
  activeMembers: number
  repeatRate: number
}

interface LevelItem {
  level: number
  count: number
  percent: number
}

interface MemberItem {
  id: string
  nickname: string
  phone: string
  avatarUrl: string
  joinDate: string
  totalAmount: number
  orderCount: number
}

const selectedDate = ref(new Date().toISOString().slice(0, 7))
const stats = ref<Stats>({ totalMembers: 0, newMembers: 0, activeMembers: 0, repeatRate: 0 })
const levelDistribution = ref<LevelItem[]>([])
const rankType = ref('amount')
const topMembers = ref<MemberItem[]>([])

const onDateChange = (e: any) => {
  selectedDate.value = e.detail.value
}

const loadStats = async () => {
  try {
    const data = await get<{
      stats: Stats
      levelDistribution: LevelItem[]
      topMembers: MemberItem[]
    }>('/v1/merchant/members/stats', {
      month: selectedDate.value,
      rankType: rankType.value
    })
    
    stats.value = data.stats
    levelDistribution.value = data.levelDistribution || []
    topMembers.value = data.topMembers || []
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const goMemberList = () => {
  uni.navigateTo({ url: '/pages/admin/member/list' })
}

watch([selectedDate, rankType], () => {
  loadStats()
})

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss">
.member-stats-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.stats-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  margin: 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  color: #fff;
  
  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title {
      font-size: 30rpx;
      font-weight: bold;
    }
    
    .date-picker {
      display: flex;
      align-items: center;
      padding: 8rpx 16rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      font-size: 26rpx;
      
      .arrow {
        margin-left: 8rpx;
        font-size: 20rpx;
      }
    }
  }
  
  .stats-body {
    .stat-item.main {
      text-align: center;
      padding-bottom: 24rpx;
      margin-bottom: 24rpx;
      border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
      
      .num {
        display: block;
        font-size: 64rpx;
        font-weight: bold;
      }
      
      .label {
        display: block;
        font-size: 26rpx;
        opacity: 0.9;
        margin-top: 8rpx;
      }
    }
    
    .stat-row {
      display: flex;
      justify-content: space-around;
      
      .stat-item {
        text-align: center;
        
        .num {
          display: block;
          font-size: 36rpx;
          font-weight: bold;
        }
        
        .label {
          display: block;
          font-size: 24rpx;
          opacity: 0.8;
          margin-top: 8rpx;
        }
      }
    }
  }
}

.section-card {
  background: #fff;
  margin: 0 24rpx 24rpx;
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
  
  .tabs {
    display: flex;
    
    text {
      margin-left: 24rpx;
      font-size: 26rpx;
      color: #999;
      
      &.active {
        color: #667eea;
        font-weight: bold;
      }
    }
  }
}

.level-chart {
  .level-bar {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .level-name {
      width: 80rpx;
      font-size: 26rpx;
      color: #666;
    }
    
    .bar-wrap {
      flex: 1;
      height: 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      overflow: hidden;
      margin: 0 16rpx;
      
      .bar {
        height: 100%;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 12rpx;
        transition: width 0.3s;
      }
    }
    
    .count {
      width: 80rpx;
      font-size: 24rpx;
      color: #999;
      text-align: right;
    }
  }
}

.rank-list {
  .rank-item {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .rank-badge {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      color: #999;
      
      &.top {
        background: linear-gradient(135deg, #ffd700, #ff9500);
        color: #fff;
        font-weight: bold;
      }
    }
    
    .avatar {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      margin-left: 16rpx;
    }
    
    .info {
      flex: 1;
      margin-left: 16rpx;
      
      .name {
        display: block;
        font-size: 28rpx;
        color: #333;
      }
      
      .join-time {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
    
    .value {
      .num {
        font-size: 30rpx;
        font-weight: bold;
        color: #667eea;
      }
    }
  }
}

.member-list-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  margin: 0 24rpx;
  padding: 24rpx;
  border-radius: 12rpx;
  
  .text {
    font-size: 28rpx;
    color: #333;
  }
  
  .arrow {
    font-size: 32rpx;
    color: #ccc;
  }
}
</style>
