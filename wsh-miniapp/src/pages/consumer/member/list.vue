<template>
  <view class="member-list-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="icon">🔍</text>
        <input 
          type="text" 
          v-model="searchKey" 
          placeholder="搜索商户名称" 
          confirm-type="search"
          @confirm="handleSearch"
        />
        <text v-if="searchKey" class="clear" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-bar">
      <text class="total">共 {{ memberList.length }} 家商户会员</text>
      <view class="sort-btn" @click="toggleSort">
        <text>{{ sortLabel }}</text>
        <text class="arrow">▼</text>
      </view>
    </view>

    <!-- 会员列表 -->
    <view class="member-list">
      <view v-for="item in filteredList" :key="item.merchantId" class="member-card" @click="goDetail(item.merchantId)">
        <image class="logo" :src="item.logoUrl || '/static/default-merchant.png'" mode="aspectFill" />
        <view class="info">
          <view class="header">
            <text class="name">{{ item.merchantName }}</text>
            <view v-if="item.vipLevel" class="vip-badge">VIP{{ item.vipLevel }}</view>
          </view>
          <view class="detail-row">
            <text class="label">会员ID: </text>
            <text class="value">{{ item.memberNo }}</text>
          </view>
          <view class="equity-summary">
            <view class="equity-item">
              <text class="num">{{ item.voucherCount }}</text>
              <text class="label">优惠券</text>
            </view>
            <view class="equity-item">
              <text class="num">¥{{ item.depositBalance }}</text>
              <text class="label">储值余额</text>
            </view>
            <view class="equity-item">
              <text class="num">{{ item.points }}</text>
              <text class="label">积分</text>
            </view>
          </view>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && filteredList.length === 0" class="empty-state">
      <image src="/static/empty-member.png" mode="aspectFit" class="empty-icon" />
      <text class="empty-text">{{ searchKey ? '未找到相关商户' : '暂无会员身份' }}</text>
      <text class="empty-hint">参与商户活动即可自动成为会员</text>
      <button class="go-btn" @click="goActivity">去发现活动</button>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get } from '@/api/request'

interface MemberItem {
  merchantId: string
  merchantName: string
  logoUrl: string
  memberNo: string
  vipLevel: number
  voucherCount: number
  depositBalance: number
  points: number
  joinDate: string
}

const loading = ref(true)
const searchKey = ref('')
const sortType = ref<'default' | 'equity' | 'recent'>('default')
const memberList = ref<MemberItem[]>([])

const sortLabel = computed(() => {
  const map: Record<string, string> = {
    default: '默认排序',
    equity: '按权益值',
    recent: '最近使用'
  }
  return map[sortType.value]
})

const filteredList = computed(() => {
  let list = [...memberList.value]
  
  // 搜索过滤
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    list = list.filter(item => 
      item.merchantName.toLowerCase().includes(key)
    )
  }
  
  // 排序
  if (sortType.value === 'equity') {
    list.sort((a, b) => (b.depositBalance + b.points * 0.01) - (a.depositBalance + a.points * 0.01))
  } else if (sortType.value === 'recent') {
    list.sort((a, b) => new Date(b.joinDate).getTime() - new Date(a.joinDate).getTime())
  }
  
  return list
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await get<{ list: MemberItem[] }>('/consumer/members')
    if (res.code === 0) {
      memberList.value = res.data.list || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 触发搜索（已通过computed自动过滤）
}

const clearSearch = () => {
  searchKey.value = ''
}

const toggleSort = () => {
  const types: Array<'default' | 'equity' | 'recent'> = ['default', 'equity', 'recent']
  const currentIndex = types.indexOf(sortType.value)
  sortType.value = types[(currentIndex + 1) % types.length]
}

const goDetail = (merchantId: string) => {
  uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${merchantId}` })
}

const goActivity = () => {
  uni.switchTab({ url: '/pages/tabbar/activity/index' })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss">
.member-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-bar {
  background: #fff;
  padding: 24rpx;
  
  .search-input {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 40rpx;
    padding: 16rpx 24rpx;
    
    .icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    input {
      flex: 1;
      font-size: 28rpx;
    }
    
    .clear {
      color: #999;
      padding: 8rpx;
    }
  }
}

.stats-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  
  .total {
    font-size: 26rpx;
    color: #666;
  }
  
  .sort-btn {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #666;
    
    .arrow {
      font-size: 20rpx;
      margin-left: 8rpx;
    }
  }
}

.member-list {
  padding: 24rpx;
}

.member-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  
  .logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }
  
  .info {
    flex: 1;
    margin-left: 24rpx;
    
    .header {
      display: flex;
      align-items: center;
      
      .name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
      
      .vip-badge {
        margin-left: 12rpx;
        padding: 4rpx 12rpx;
        background: linear-gradient(135deg, #ffd700, #ff9500);
        border-radius: 12rpx;
        font-size: 20rpx;
        color: #fff;
      }
    }
    
    .detail-row {
      margin-top: 12rpx;
      font-size: 24rpx;
      
      .label {
        color: #999;
      }
      
      .value {
        color: #666;
      }
    }
    
    .equity-summary {
      display: flex;
      margin-top: 16rpx;
      
      .equity-item {
        margin-right: 32rpx;
        
        .num {
          display: block;
          font-size: 28rpx;
          font-weight: bold;
          color: #ff6b35;
        }
        
        .label {
          display: block;
          font-size: 22rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }
    }
  }
  
  .arrow {
    font-size: 36rpx;
    color: #ccc;
    flex-shrink: 0;
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
    font-size: 32rpx;
    color: #999;
    margin-top: 32rpx;
  }
  
  .empty-hint {
    font-size: 26rpx;
    color: #bbb;
    margin-top: 16rpx;
  }
  
  .go-btn {
    margin-top: 40rpx;
    padding: 20rpx 60rpx;
    background: #ff6b35;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
    border: none;
  }
}

.loading-state {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
