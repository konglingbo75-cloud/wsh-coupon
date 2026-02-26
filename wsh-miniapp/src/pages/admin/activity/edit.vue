<template>
  <view class="activity-edit-page">
    <!-- 顶部导航栏 -->
    <view class="page-header">
      <text class="page-title">{{ isEditMode ? '编辑活动' : '新建活动' }}</text>
    </view>

    <!-- 表单区域 -->
    <view class="form-container">
      <!-- 基础信息 -->
      <view class="form-section">
        <view class="section-title">基础信息</view>
        
        <!-- 活动标题 -->
        <view class="form-item required">
          <text class="label">活动标题</text>
          <input 
            v-model="formData.title" 
            class="input" 
            placeholder="请输入活动标题" 
            maxlength="50"
          />
        </view>

        <!-- 活动类型 -->
        <view class="form-item required">
          <text class="label">活动类型</text>
          <radio-group class="radio-group" @change="handleTypeChange">
            <label v-for="type in activityTypes" :key="type.value" class="radio-item">
              <radio :value="type.value" :checked="formData.type === type.value" />
              <text class="radio-label">{{ type.label }}</text>
            </label>
          </radio-group>
        </view>

        <!-- 封面图片 -->
        <view class="form-item required">
          <text class="label">封面图片</text>
          <view class="image-picker">
            <image 
              v-if="formData.coverUrl" 
              :src="formData.coverUrl" 
              class="preview-image" 
              mode="aspectFill"
            />
            <input 
              v-model="formData.coverUrl" 
              class="input" 
              placeholder="请输入图片URL（开发环境）" 
            />
            <!-- TODO: 生产环境集成图片上传 -->
          </view>
        </view>

        <!-- 价格 -->
        <view class="form-item required">
          <text class="label">售价(元)</text>
          <input 
            v-model.number="formData.price" 
            type="digit" 
            class="input" 
            placeholder="请输入售价" 
          />
        </view>

        <!-- 原价 -->
        <view class="form-item">
          <text class="label">原价(元)</text>
          <input 
            v-model.number="formData.originalPrice" 
            type="digit" 
            class="input" 
            placeholder="选填，用于显示优惠" 
          />
        </view>

        <!-- 库存 -->
        <view class="form-item required">
          <text class="label">库存数量</text>
          <input 
            v-model.number="formData.stock" 
            type="number" 
            class="input" 
            placeholder="输入-1表示不限库存" 
          />
          <text class="hint">-1表示不限库存</text>
        </view>
      </view>

      <!-- 类型特定配置 -->
      <view class="form-section" v-if="formData.type">
        <view class="section-title">{{ getTypeConfigTitle() }}</view>

        <!-- 代金券配置 -->
        <template v-if="formData.type === 1">
          <view class="form-item required">
            <text class="label">券面值(元)</text>
            <input 
              v-model.number="typeConfig.faceValue" 
              type="digit" 
              class="input" 
              placeholder="请输入券面值" 
            />
          </view>
          <view class="form-item required">
            <text class="label">最低消费(元)</text>
            <input 
              v-model.number="typeConfig.minConsume" 
              type="digit" 
              class="input" 
              placeholder="请输入最低消费金额" 
            />
          </view>
        </template>

        <!-- 积分兑换配置 -->
        <template v-if="formData.type === 3">
          <view class="form-item required">
            <text class="label">所需积分</text>
            <input 
              v-model.number="typeConfig.requiredPoints" 
              type="number" 
              class="input" 
              placeholder="请输入所需积分" 
            />
          </view>
        </template>

        <!-- 团购配置 -->
        <template v-if="formData.type === 4">
          <view class="form-item required">
            <text class="label">成团人数</text>
            <input 
              v-model.number="typeConfig.groupSize" 
              type="number" 
              class="input" 
              placeholder="最少成团人数（如3）" 
            />
          </view>
          <view class="form-item">
            <text class="label">最大参团人数</text>
            <input 
              v-model.number="typeConfig.maxMembers" 
              type="number" 
              class="input" 
              placeholder="选填，默认与成团人数相同" 
            />
            <text class="hint">允许超过成团人数继续加入</text>
          </view>
          <view class="form-item">
            <text class="label">拼团有效期(小时)</text>
            <input 
              v-model.number="typeConfig.expireHours" 
              type="number" 
              class="input" 
              placeholder="默认24小时" 
            />
            <text class="hint">超时未成团将自动关闭</text>
          </view>
          <view class="form-item">
            <text class="label">超时自动退款</text>
            <switch :checked="typeConfig.autoRefund" @change="(e: any) => typeConfig.autoRefund = e.detail.value" color="#ff6b35" />
            <text class="hint">拼团失败后自动退款给参与者</text>
          </view>
        </template>
      </view>

      <!-- 活动详情 -->
      <view class="form-section">
        <view class="section-title">活动详情</view>

        <!-- 活动描述 -->
        <view class="form-item">
          <text class="label">活动描述</text>
          <textarea 
            v-model="formData.description" 
            class="textarea" 
            placeholder="请输入活动描述"
            maxlength="500"
          />
        </view>

        <!-- 开始时间 -->
        <view class="form-item required">
          <text class="label">开始时间</text>
          <picker 
            mode="date" 
            :value="formData.startTime" 
            @change="handleStartTimeChange"
          >
            <view class="picker">
              {{ formData.startTime || '请选择开始时间' }}
            </view>
          </picker>
        </view>

        <!-- 结束时间 -->
        <view class="form-item required">
          <text class="label">结束时间</text>
          <picker 
            mode="date" 
            :value="formData.endTime" 
            :start="formData.startTime"
            @change="handleEndTimeChange"
          >
            <view class="picker">
              {{ formData.endTime || '请选择结束时间' }}
            </view>
          </picker>
        </view>

        <!-- 是否公开 -->
        <view class="form-item">
          <text class="label">是否公开</text>
          <switch :checked="formData.isPublic" @change="handlePublicChange" color="#ff6b35" />
          <text class="hint">关闭后用户无法在列表看到此活动</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="footer-actions">
      <button class="action-btn draft" @click="handleSaveDraft" :loading="saving">
        保存草稿
      </button>
      <button class="action-btn publish" @click="handlePublish" :loading="publishing">
        {{ isEditMode ? '保存并发布' : '立即发布' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { get, post } from '@/api/request'

// 活动类型选项
const activityTypes = [
  { value: 1, label: '代金券' },
  { value: 3, label: '积分兑换' },
  { value: 4, label: '团购' }
]

// 判断是否为编辑模式
const isEditMode = ref(false)
const activityId = ref('')

// 表单数据
const formData = reactive({
  title: '',
  type: null as number | null,
  coverUrl: '',
  price: null as number | null,
  originalPrice: null as number | null,
  stock: null as number | null,
  description: '',
  startTime: '',
  endTime: '',
  isPublic: true
})

// 类型特定配置
const typeConfig = reactive({
  // 代金券
  faceValue: null as number | null,
  minConsume: null as number | null,
  // 积分兑换
  requiredPoints: null as number | null,
  // 团购
  groupSize: null as number | null,
  maxMembers: null as number | null,
  expireHours: null as number | null,
  autoRefund: true
})

// 加载状态
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)

// 获取类型配置标题
const getTypeConfigTitle = () => {
  const map: Record<number, string> = {
    1: '代金券配置',
    3: '积分兑换配置',
    4: '团购配置'
  }
  return formData.type ? map[formData.type] : ''
}

// 处理类型变更
const handleTypeChange = (e: any) => {
  formData.type = parseInt(e.detail.value)
  // 清空之前的类型配置
  typeConfig.faceValue = null
  typeConfig.minConsume = null
  typeConfig.requiredPoints = null
  typeConfig.groupSize = null
  typeConfig.maxMembers = null
  typeConfig.expireHours = null
  typeConfig.autoRefund = true
}

// 处理开始时间变更
const handleStartTimeChange = (e: any) => {
  formData.startTime = e.detail.value
}

// 处理结束时间变更
const handleEndTimeChange = (e: any) => {
  formData.endTime = e.detail.value
}

// 处理公开状态变更
const handlePublicChange = (e: any) => {
  formData.isPublic = e.detail.value
}

// 表单验证
const validateForm = (isDraft = false) => {
  // 草稿模式下只验证必填的基础信息
  if (!formData.title?.trim()) {
    uni.showToast({ title: '请输入活动标题', icon: 'none' })
    return false
  }

  if (!formData.type) {
    uni.showToast({ title: '请选择活动类型', icon: 'none' })
    return false
  }

  // 发布模式下验证完整信息
  if (!isDraft) {
    if (!formData.coverUrl?.trim()) {
      uni.showToast({ title: '请输入封面图片', icon: 'none' })
      return false
    }

    if (formData.price === null || formData.price < 0) {
      uni.showToast({ title: '请输入有效的售价', icon: 'none' })
      return false
    }

    if (formData.stock === null) {
      uni.showToast({ title: '请输入库存数量', icon: 'none' })
      return false
    }

    if (!formData.startTime) {
      uni.showToast({ title: '请选择开始时间', icon: 'none' })
      return false
    }

    if (!formData.endTime) {
      uni.showToast({ title: '请选择结束时间', icon: 'none' })
      return false
    }

    if (formData.endTime <= formData.startTime) {
      uni.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' })
      return false
    }

    // 验证类型特定配置
    if (formData.type === 1) {
      if (typeConfig.faceValue === null || typeConfig.faceValue <= 0) {
        uni.showToast({ title: '请输入有效的券面值', icon: 'none' })
        return false
      }
      if (typeConfig.minConsume === null || typeConfig.minConsume < 0) {
        uni.showToast({ title: '请输入有效的最低消费', icon: 'none' })
        return false
      }
    }

    if (formData.type === 3) {
      if (typeConfig.requiredPoints === null || typeConfig.requiredPoints <= 0) {
        uni.showToast({ title: '请输入有效的所需积分', icon: 'none' })
        return false
      }
    }

    if (formData.type === 4) {
      if (typeConfig.groupSize === null || typeConfig.groupSize < 2) {
        uni.showToast({ title: '成团人数至少为2人', icon: 'none' })
        return false
      }
    }
  }

  return true
}

// 构建提交数据
const buildSubmitData = (status: string) => {
  const data: any = {
    title: formData.title,
    activityType: formData.type,
    coverUrl: formData.coverUrl,
    price: formData.price,
    originalPrice: formData.originalPrice,
    stock: formData.stock,
    description: formData.description,
    startTime: formData.startTime,
    endTime: formData.endTime,
    isPublic: formData.isPublic,
    status
  }

  // 添加类型特定配置
  if (formData.type === 1) {
    data.typeConfig = {
      faceValue: typeConfig.faceValue,
      minConsume: typeConfig.minConsume
    }
  } else if (formData.type === 3) {
    data.typeConfig = {
      requiredPoints: typeConfig.requiredPoints
    }
  } else if (formData.type === 4) {
    data.typeConfig = {
      groupSize: typeConfig.groupSize,
      maxMembers: typeConfig.maxMembers || typeConfig.groupSize,
      expireHours: typeConfig.expireHours || 24,
      autoRefund: typeConfig.autoRefund ? 1 : 0
    }
  }

  return data
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!validateForm(true)) return

  saving.value = true
  try {
    const data = buildSubmitData('draft')

    if (isEditMode.value) {
      await post(`/v1/merchant/activities/${activityId.value}`, data)
    } else {
      await post('/v1/merchant/activities', data)
    }

    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e: any) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

// 发布
const handlePublish = async () => {
  if (!validateForm(false)) return

  publishing.value = true
  try {
    const data = buildSubmitData('active')

    if (isEditMode.value) {
      await post(`/v1/merchant/activities/${activityId.value}`, data)
    } else {
      await post('/v1/merchant/activities', data)
    }

    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e: any) {
    uni.showToast({ title: e.message || '发布失败', icon: 'none' })
  } finally {
    publishing.value = false
  }
}

// 加载活动详情（编辑模式）
const loadActivity = async () => {
  if (!activityId.value) return

  loading.value = true
  try {
    const data = await get(`/v1/merchant/activities/${activityId.value}`)
    
    // data 已被 request 封装解包，直接使用
    formData.title = data.title
    formData.type = data.activityType
    formData.coverUrl = data.coverUrl
    formData.price = data.price
    formData.originalPrice = data.originalPrice
    formData.stock = data.stock
    formData.description = data.description
    formData.startTime = data.startTime
    formData.endTime = data.endTime
    formData.isPublic = data.isPublic

    // 填充类型配置
    if (data.typeConfig) {
      if (data.activityType === 1) {
        typeConfig.faceValue = data.typeConfig.faceValue
        typeConfig.minConsume = data.typeConfig.minConsume
      } else if (data.activityType === 3) {
        typeConfig.requiredPoints = data.typeConfig.requiredPoints
      } else if (data.activityType === 4) {
        typeConfig.groupSize = data.typeConfig.groupSize
        typeConfig.maxMembers = data.typeConfig.maxMembers
        typeConfig.expireHours = data.typeConfig.expireHours
        typeConfig.autoRefund = data.typeConfig.autoRefund !== 0
      }
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } finally {
    loading.value = false
  }
}

// 页面加载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.options || {}

  if (options.id) {
    isEditMode.value = true
    activityId.value = options.id
    loadActivity()
  }
})
</script>

<style lang="scss">
.activity-edit-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.page-header {
  background: #fff;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
}

.form-container {
  padding: 24rpx;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 2rpx solid #f5f5f5;
  }
}

.form-item {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &.required .label::before {
    content: '*';
    color: #ff6b35;
    margin-right: 4rpx;
  }
  
  .label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;
    font-weight: 500;
  }
  
  .input {
    width: 100%;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: 1rpx solid transparent;
    
    &:focus {
      background: #fff;
      border-color: #ff6b35;
    }
  }
  
  .textarea {
    width: 100%;
    min-height: 200rpx;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: 1rpx solid transparent;
    
    &:focus {
      background: #fff;
      border-color: #ff6b35;
    }
  }
  
  .picker {
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #333;
  }
  
  .hint {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 8rpx;
  }
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  
  .radio-item {
    display: flex;
    align-items: center;
    margin-right: 40rpx;
    margin-bottom: 16rpx;
    
    .radio-label {
      margin-left: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.image-picker {
  .preview-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    display: block;
  }
}

.footer-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  z-index: 100;
  
  .action-btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 32rpx;
    border-radius: 44rpx;
    border: none;
    
    &.draft {
      background: #f5f5f5;
      color: #666;
      margin-right: 16rpx;
    }
    
    &.publish {
      background: linear-gradient(135deg, #ff6b35, #f7931e);
      color: #fff;
    }
  }
}
</style>
