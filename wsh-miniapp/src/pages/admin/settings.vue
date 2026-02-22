<template>
  <view class="settings-page">
    <!-- 商户信息 -->
    <view class="section">
      <view class="section-header">
        <text class="title">商户信息</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="goEditMerchant">
          <text class="label">商户资料</text>
          <text class="value">{{ merchantInfo?.name }}</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="goBusinessHours">
          <text class="label">营业时间</text>
          <text class="value">{{ merchantInfo?.businessHours || '未设置' }}</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="goStoreList">
          <text class="label">门店管理</text>
          <text class="value">{{ merchantInfo?.storeCount || 0 }}家</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 员工管理 -->
    <view class="section">
      <view class="section-header">
        <text class="title">员工管理</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="goEmployeeList">
          <text class="label">员工列表</text>
          <text class="value">{{ employeeCount }}人</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="goBindCode">
          <text class="label">员工绑定码</text>
          <text class="value">查看</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 通知设置 -->
    <view class="section">
      <view class="section-header">
        <text class="title">通知设置</text>
      </view>
      <view class="menu-list">
        <view class="menu-item">
          <text class="label">新订单提醒</text>
          <switch :checked="notifySettings.newOrder" @change="toggleNotify('newOrder', $event)" color="#ff6b35" />
        </view>
        <view class="menu-item">
          <text class="label">核销提醒</text>
          <switch :checked="notifySettings.verify" @change="toggleNotify('verify', $event)" color="#ff6b35" />
        </view>
        <view class="menu-item">
          <text class="label">账单提醒</text>
          <switch :checked="notifySettings.billing" @change="toggleNotify('billing', $event)" color="#ff6b35" />
        </view>
      </view>
    </view>

    <!-- 账户安全 -->
    <view class="section">
      <view class="section-header">
        <text class="title">账户安全</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="goChangePassword">
          <text class="label">修改密码</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="goBindPhone">
          <text class="label">绑定手机</text>
          <text class="value">{{ maskPhone(merchantInfo?.phone) }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 其他 -->
    <view class="section">
      <view class="section-header">
        <text class="title">其他</text>
      </view>
      <view class="menu-list">
        <view class="menu-item" @click="goAgreement">
          <text class="label">商户协议</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="goAbout">
          <text class="label">关于我们</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @click="contactService">
          <text class="label">联系客服</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出按钮 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post } from '@/api/request'
import { useUserStore } from '@/store/user'

interface MerchantInfo {
  id: string
  name: string
  phone: string
  businessHours?: string
  storeCount: number
}

interface NotifySettings {
  newOrder: boolean
  verify: boolean
  billing: boolean
}

const userStore = useUserStore()

const merchantInfo = ref<MerchantInfo | null>(null)
const employeeCount = ref(0)
const notifySettings = ref<NotifySettings>({
  newOrder: true,
  verify: true,
  billing: true
})

const maskPhone = (phone?: string) => {
  if (!phone || phone.length !== 11) return phone || '未绑定'
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

const loadSettings = async () => {
  try {
    const res = await get<{
      merchant: MerchantInfo
      employeeCount: number
      notifySettings: NotifySettings
    }>('/admin/settings')
    
    if (res.code === 0) {
      merchantInfo.value = res.data.merchant
      employeeCount.value = res.data.employeeCount
      notifySettings.value = res.data.notifySettings
    }
  } catch (e) {
    // 忽略
  }
}

const toggleNotify = async (key: keyof NotifySettings, e: any) => {
  const value = e.detail.value
  notifySettings.value[key] = value
  
  try {
    await post('/admin/settings/notify', { [key]: value })
  } catch (e) {
    // 回滚
    notifySettings.value[key] = !value
    uni.showToast({ title: '设置失败', icon: 'none' })
  }
}

const goEditMerchant = () => {
  uni.navigateTo({ url: '/pages/admin/settings/merchant-edit' })
}

const goBusinessHours = () => {
  uni.navigateTo({ url: '/pages/admin/settings/business-hours' })
}

const goStoreList = () => {
  uni.navigateTo({ url: '/pages/admin/settings/store-list' })
}

const goEmployeeList = () => {
  uni.navigateTo({ url: '/pages/admin/settings/employee-list' })
}

const goBindCode = () => {
  uni.navigateTo({ url: '/pages/admin/settings/bind-code' })
}

const goChangePassword = () => {
  uni.navigateTo({ url: '/pages/admin/settings/change-password' })
}

const goBindPhone = () => {
  uni.navigateTo({ url: '/pages/admin/settings/bind-phone' })
}

const goAgreement = () => {
  uni.navigateTo({ url: '/pages/admin/settings/agreement' })
}

const goAbout = () => {
  uni.navigateTo({ url: '/pages/admin/settings/about' })
}

const contactService = () => {
  uni.makePhoneCall({ phoneNumber: '400-xxx-xxxx' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await userStore.logout()
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}

onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss">
.settings-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.section {
  background: #fff;
  margin-bottom: 24rpx;
}

.section-header {
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  .title {
    font-size: 28rpx;
    color: #999;
  }
}

.menu-list {
  .menu-item {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      flex: 1;
      font-size: 30rpx;
      color: #333;
    }
    
    .value {
      font-size: 28rpx;
      color: #999;
      margin-right: 12rpx;
    }
    
    .arrow {
      font-size: 28rpx;
      color: #ccc;
    }
  }
}

.logout-btn {
  margin: 48rpx 24rpx;
  padding: 24rpx;
  background: #fff;
  color: #f44336;
  font-size: 32rpx;
  border-radius: 12rpx;
  border: none;
}
</style>
