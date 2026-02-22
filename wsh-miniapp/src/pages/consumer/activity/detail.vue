<template>
  <view class="activity-detail-page">
    <!-- 活动封面 -->
    <view class="cover-section">
      <swiper class="cover-swiper" :indicator-dots="activity?.images?.length > 1" indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
        <swiper-item v-for="(img, index) in (activity?.images || [activity?.coverUrl])" :key="index">
          <image class="cover-image" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>
      <view class="price-tag">
        <text class="currency">¥</text>
        <text class="price">{{ activity?.price }}</text>
        <text v-if="activity?.originalPrice" class="original">¥{{ activity?.originalPrice }}</text>
      </view>
    </view>

    <!-- 活动信息 -->
    <view class="activity-info">
      <text class="title">{{ activity?.title }}</text>
      <view class="tags">
        <text class="tag type">{{ getTypeName(activity?.type) }}</text>
        <text v-if="activity?.isHot" class="tag hot">热门</text>
        <text v-if="activity?.isNew" class="tag new">新品</text>
      </view>
      <view class="stats">
        <text class="sold">已售 {{ activity?.soldCount || 0 }}</text>
        <text class="remain">剩余 {{ activity?.remainStock || 0 }}</text>
      </view>
    </view>

    <!-- 商户信息 -->
    <view class="merchant-section" @click="goMerchant">
      <image class="logo" :src="activity?.merchant?.logoUrl || '/static/default-merchant.png'" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ activity?.merchant?.name }}</text>
        <text class="address">{{ activity?.merchant?.address }}</text>
      </view>
      <text class="arrow">›</text>
    </view>

    <!-- 活动规则 -->
    <view class="rules-section">
      <view class="section-header">
        <text class="title">活动规则</text>
      </view>
      <view class="rule-list">
        <view class="rule-item">
          <text class="label">有效期</text>
          <text class="value">{{ activity?.validStart }} 至 {{ activity?.validEnd }}</text>
        </view>
        <view class="rule-item">
          <text class="label">使用时间</text>
          <text class="value">{{ activity?.useTimeDesc || '营业时间内可用' }}</text>
        </view>
        <view class="rule-item">
          <text class="label">适用门店</text>
          <text class="value">{{ activity?.applicableStores || '全部门店' }}</text>
        </view>
        <view v-if="activity?.minConsume" class="rule-item">
          <text class="label">最低消费</text>
          <text class="value">满{{ activity?.minConsume }}元可用</text>
        </view>
        <view class="rule-item">
          <text class="label">叠加规则</text>
          <text class="value">{{ activity?.stackable ? '可与其他优惠叠加' : '不可叠加使用' }}</text>
        </view>
      </view>
    </view>

    <!-- 活动详情 -->
    <view class="detail-section">
      <view class="section-header">
        <text class="title">活动详情</text>
      </view>
      <view class="detail-content">
        <rich-text :nodes="activity?.detailHtml || activity?.description || '暂无详情'"></rich-text>
      </view>
    </view>

    <!-- 用户评价 -->
    <view v-if="reviews.length > 0" class="review-section">
      <view class="section-header">
        <text class="title">用户评价 ({{ reviewCount }})</text>
        <view class="more" @click="goReviews">
          <text>查看全部</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="review-list">
        <view v-for="item in reviews.slice(0, 2)" :key="item.id" class="review-item">
          <view class="user-info">
            <image class="avatar" :src="item.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="info">
              <text class="nickname">{{ item.nickname }}</text>
              <text class="time">{{ item.time }}</text>
            </view>
            <view class="rating">
              <text v-for="i in 5" :key="i" class="star" :class="{ active: i <= item.rating }">★</text>
            </view>
          </view>
          <text class="content">{{ item.content }}</text>
        </view>
      </view>
    </view>

    <!-- 底部购买栏 -->
    <view class="bottom-bar">
      <view class="actions">
        <view class="action-item" @click="toggleFavorite">
          <text class="icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
          <text class="label">收藏</text>
        </view>
        <view class="action-item" @click="shareActivity">
          <text class="icon">📤</text>
          <text class="label">分享</text>
        </view>
        <view class="action-item" @click="callMerchant">
          <text class="icon">📞</text>
          <text class="label">客服</text>
        </view>
      </view>
      <button 
        class="buy-btn" 
        :disabled="!canBuy" 
        @click="handleBuy"
      >
        {{ buyBtnText }}
      </button>
    </view>

    <!-- 数量选择弹窗 -->
    <view v-if="showQuantityModal" class="quantity-modal" @click="showQuantityModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="title">选择数量</text>
          <text class="close" @click="showQuantityModal = false">✕</text>
        </view>
        <view class="modal-body">
          <view class="activity-brief">
            <image class="cover" :src="activity?.coverUrl" mode="aspectFill" />
            <view class="info">
              <text class="title">{{ activity?.title }}</text>
              <text class="price">¥{{ activity?.price }}</text>
            </view>
          </view>
          <view class="quantity-selector">
            <text class="label">数量</text>
            <view class="selector">
              <text class="btn" :class="{ disabled: quantity <= 1 }" @click="changeQuantity(-1)">-</text>
              <text class="num">{{ quantity }}</text>
              <text class="btn" :class="{ disabled: quantity >= maxQuantity }" @click="changeQuantity(1)">+</text>
            </view>
          </view>
          <view class="limit-hint" v-if="activity?.limitPerUser">
            <text>每人限购 {{ activity?.limitPerUser }} 份</text>
          </view>
        </view>
        <view class="modal-footer">
          <text class="total">合计: ¥{{ (activity?.price || 0) * quantity }}</text>
          <button class="confirm-btn" @click="confirmBuy">确认购买</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { get, post } from '@/api/request'
import { useUserStore } from '@/store/user'

interface ActivityDetail {
  id: string
  title: string
  type: 'voucher' | 'deposit' | 'points' | 'group'
  price: number
  originalPrice?: number
  coverUrl: string
  images?: string[]
  description?: string
  detailHtml?: string
  soldCount: number
  remainStock: number
  validStart: string
  validEnd: string
  useTimeDesc?: string
  applicableStores?: string
  minConsume?: number
  stackable: boolean
  limitPerUser?: number
  isHot?: boolean
  isNew?: boolean
  merchant: {
    id: string
    name: string
    logoUrl: string
    address: string
    phone: string
  }
}

interface ReviewItem {
  id: string
  nickname: string
  avatarUrl: string
  rating: number
  content: string
  time: string
}

const userStore = useUserStore()
const activityId = ref('')
const activity = ref<ActivityDetail | null>(null)
const reviews = ref<ReviewItem[]>([])
const reviewCount = ref(0)
const isFavorite = ref(false)
const showQuantityModal = ref(false)
const quantity = ref(1)

const maxQuantity = computed(() => {
  const limit = activity.value?.limitPerUser || 99
  const remain = activity.value?.remainStock || 0
  return Math.min(limit, remain)
})

const canBuy = computed(() => {
  return activity.value && activity.value.remainStock > 0
})

const buyBtnText = computed(() => {
  if (!activity.value) return '加载中...'
  if (activity.value.remainStock <= 0) return '已售罄'
  return `¥${activity.value.price} 立即购买`
})

const getTypeName = (type?: string) => {
  const map: Record<string, string> = {
    voucher: '优惠券',
    deposit: '储值',
    points: '积分',
    group: '团购'
  }
  return map[type || ''] || type
}

const loadData = async () => {
  try {
    const res = await get<{
      activity: ActivityDetail
      reviews: ReviewItem[]
      reviewCount: number
      isFavorite: boolean
    }>(`/public/activities/${activityId.value}`)
    
    if (res.code === 0) {
      activity.value = res.data.activity
      reviews.value = res.data.reviews || []
      reviewCount.value = res.data.reviewCount || 0
      isFavorite.value = res.data.isFavorite || false
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const goMerchant = () => {
  if (activity.value?.merchant?.id) {
    uni.navigateTo({ url: `/pages/consumer/member/detail?merchantId=${activity.value.merchant.id}` })
  }
}

const goReviews = () => {
  uni.navigateTo({ url: `/pages/consumer/activity/reviews?activityId=${activityId.value}` })
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  
  try {
    const res = await post(`/consumer/favorites/${activityId.value}`, {
      action: isFavorite.value ? 'remove' : 'add'
    })
    if (res.code === 0) {
      isFavorite.value = !isFavorite.value
      uni.showToast({ title: isFavorite.value ? '已收藏' : '已取消收藏', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const shareActivity = () => {
  // 微信小程序分享
}

const callMerchant = () => {
  if (activity.value?.merchant?.phone) {
    uni.makePhoneCall({ phoneNumber: activity.value.merchant.phone })
  } else {
    uni.showToast({ title: '暂无客服电话', icon: 'none' })
  }
}

const handleBuy = () => {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  
  if (!canBuy.value) return
  
  showQuantityModal.value = true
}

const changeQuantity = (delta: number) => {
  const newVal = quantity.value + delta
  if (newVal >= 1 && newVal <= maxQuantity.value) {
    quantity.value = newVal
  }
}

const confirmBuy = () => {
  showQuantityModal.value = false
  uni.navigateTo({
    url: `/pages/consumer/order/confirm?activityId=${activityId.value}&quantity=${quantity.value}`
  })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  activityId.value = currentPage.options?.id || ''
  if (activityId.value) {
    loadData()
  }
})
</script>

<style lang="scss">
.activity-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

.cover-section {
  position: relative;
  
  .cover-swiper {
    height: 500rpx;
    
    .cover-image {
      width: 100%;
      height: 100%;
    }
  }
  
  .price-tag {
    position: absolute;
    bottom: 24rpx;
    left: 24rpx;
    background: rgba(0, 0, 0, 0.6);
    padding: 12rpx 24rpx;
    border-radius: 8rpx;
    display: flex;
    align-items: baseline;
    
    .currency {
      font-size: 28rpx;
      color: #ff6b35;
    }
    
    .price {
      font-size: 48rpx;
      font-weight: bold;
      color: #ff6b35;
      margin-left: 4rpx;
    }
    
    .original {
      font-size: 24rpx;
      color: #999;
      text-decoration: line-through;
      margin-left: 16rpx;
    }
  }
}

.activity-info {
  background: #fff;
  padding: 24rpx;
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
  }
  
  .tags {
    display: flex;
    margin-top: 16rpx;
    
    .tag {
      padding: 6rpx 16rpx;
      border-radius: 6rpx;
      font-size: 22rpx;
      margin-right: 12rpx;
      
      &.type {
        background: #fff3e0;
        color: #f7931e;
      }
      
      &.hot {
        background: #ffebee;
        color: #f44336;
      }
      
      &.new {
        background: #e8f5e9;
        color: #4caf50;
      }
    }
  }
  
  .stats {
    display: flex;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;
    
    .sold {
      margin-right: 24rpx;
    }
  }
}

.merchant-section {
  display: flex;
  align-items: center;
  background: #fff;
  margin-top: 16rpx;
  padding: 24rpx;
  
  .logo {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
  }
  
  .info {
    flex: 1;
    margin-left: 20rpx;
    
    .name {
      display: block;
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }
    
    .address {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
  
  .arrow {
    font-size: 32rpx;
    color: #ccc;
  }
}

.rules-section, .detail-section, .review-section {
  background: #fff;
  margin-top: 16rpx;
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .title {
    font-size: 32rpx;
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

.rule-list {
  .rule-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
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

.detail-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.review-list {
  .review-item {
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      
      .avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
      }
      
      .info {
        flex: 1;
        margin-left: 16rpx;
        
        .nickname {
          display: block;
          font-size: 26rpx;
          color: #333;
        }
        
        .time {
          display: block;
          font-size: 22rpx;
          color: #999;
        }
      }
      
      .rating {
        .star {
          font-size: 24rpx;
          color: #ddd;
          
          &.active {
            color: #ffc107;
          }
        }
      }
    }
    
    .content {
      display: block;
      margin-top: 12rpx;
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .actions {
    display: flex;
    
    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-right: 32rpx;
      
      .icon {
        font-size: 36rpx;
      }
      
      .label {
        font-size: 20rpx;
        color: #666;
        margin-top: 4rpx;
      }
    }
  }
  
  .buy-btn {
    flex: 1;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 40rpx;
    padding: 20rpx 0;
    border: none;
    
    &[disabled] {
      background: #ccc;
    }
  }
}

.quantity-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
  
  .modal-content {
    width: 100%;
    background: #fff;
    border-radius: 24rpx 24rpx 0 0;
    
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
        padding: 8rpx;
      }
    }
    
    .modal-body {
      padding: 24rpx;
      
      .activity-brief {
        display: flex;
        align-items: center;
        
        .cover {
          width: 160rpx;
          height: 160rpx;
          border-radius: 12rpx;
        }
        
        .info {
          margin-left: 20rpx;
          
          .title {
            display: block;
            font-size: 28rpx;
            color: #333;
          }
          
          .price {
            display: block;
            font-size: 36rpx;
            font-weight: bold;
            color: #ff6b35;
            margin-top: 12rpx;
          }
        }
      }
      
      .quantity-selector {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 32rpx;
        padding-top: 24rpx;
        border-top: 1rpx solid #f0f0f0;
        
        .label {
          font-size: 30rpx;
          color: #333;
        }
        
        .selector {
          display: flex;
          align-items: center;
          
          .btn {
            width: 60rpx;
            height: 60rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f5f5f5;
            border-radius: 8rpx;
            font-size: 36rpx;
            color: #333;
            
            &.disabled {
              color: #ccc;
            }
          }
          
          .num {
            width: 80rpx;
            text-align: center;
            font-size: 32rpx;
            font-weight: bold;
          }
        }
      }
      
      .limit-hint {
        margin-top: 16rpx;
        font-size: 24rpx;
        color: #999;
        text-align: right;
      }
    }
    
    .modal-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24rpx;
      padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
      border-top: 1rpx solid #f0f0f0;
      
      .total {
        font-size: 32rpx;
        color: #ff6b35;
        font-weight: bold;
      }
      
      .confirm-btn {
        background: linear-gradient(135deg, #ff6b35, #f7931e);
        color: #fff;
        font-size: 28rpx;
        padding: 16rpx 48rpx;
        border-radius: 32rpx;
        border: none;
      }
    }
  }
}
</style>
