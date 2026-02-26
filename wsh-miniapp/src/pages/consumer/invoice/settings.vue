<template>
  <view class="settings-page">
    <!-- 抬头列表 -->
    <view class="settings-list">
      <view v-if="loading" class="loading-wrapper">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <image src="/static/images/empty-invoice.png" mode="aspectFit" class="empty-image" />
        <text class="empty-text">暂无发票抬头</text>
        <text class="empty-tip">点击下方按钮添加</text>
      </view>

      <view v-else>
        <view 
          v-for="item in list" 
          :key="item.settingId"
          class="setting-card"
        >
          <view class="card-main" @click="editSetting(item)">
            <view class="title-row">
              <view class="type-tag">{{ item.titleTypeName }}</view>
              <text class="title-text">{{ item.invoiceTitle }}</text>
              <view v-if="item.isDefault === 1" class="default-tag">默认</view>
            </view>
            <view v-if="item.taxNumber" class="info-row">
              <text class="label">税号</text>
              <text class="value">{{ item.taxNumber }}</text>
            </view>
            <view v-if="item.bankName" class="info-row">
              <text class="label">开户行</text>
              <text class="value">{{ item.bankName }}</text>
            </view>
          </view>
          
          <view class="card-actions">
            <view 
              v-if="item.isDefault !== 1"
              class="action-btn"
              @click="setDefault(item)"
            >
              设为默认
            </view>
            <view class="action-btn edit" @click="editSetting(item)">编辑</view>
            <view class="action-btn delete" @click="deleteSetting(item)">删除</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部添加按钮 -->
    <view class="bottom-bar">
      <view class="btn-add" @click="addSetting">
        <text class="icon">+</text>
        <text>添加发票抬头</text>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view v-if="showEditModal" class="modal-mask" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ editingItem ? '编辑抬头' : '新增抬头' }}</text>
          <view class="close-btn" @click="closeModal">×</view>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">抬头类型</text>
            <view class="type-selector">
              <view 
                :class="['type-option', { active: form.titleType === 1 }]"
                @click="form.titleType = 1"
              >
                个人
              </view>
              <view 
                :class="['type-option', { active: form.titleType === 2 }]"
                @click="form.titleType = 2"
              >
                企业
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">发票抬头 <text class="required">*</text></text>
            <input 
              v-model="form.invoiceTitle"
              class="form-input"
              placeholder="请输入发票抬头"
            />
          </view>

          <view v-if="form.titleType === 2" class="form-item">
            <text class="form-label">税号 <text class="required">*</text></text>
            <input 
              v-model="form.taxNumber"
              class="form-input"
              placeholder="请输入纳税人识别号"
            />
          </view>

          <view v-if="form.titleType === 2" class="form-item">
            <text class="form-label">开户银行</text>
            <input 
              v-model="form.bankName"
              class="form-input"
              placeholder="请输入开户银行"
            />
          </view>

          <view v-if="form.titleType === 2" class="form-item">
            <text class="form-label">银行账号</text>
            <input 
              v-model="form.bankAccount"
              class="form-input"
              placeholder="请输入银行账号"
            />
          </view>

          <view v-if="form.titleType === 2" class="form-item">
            <text class="form-label">公司地址</text>
            <input 
              v-model="form.companyAddress"
              class="form-input"
              placeholder="请输入公司地址"
            />
          </view>

          <view v-if="form.titleType === 2" class="form-item">
            <text class="form-label">公司电话</text>
            <input 
              v-model="form.companyPhone"
              class="form-input"
              placeholder="请输入公司电话"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeModal">取消</view>
          <view class="btn-confirm" @click="submitForm">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { 
  getInvoiceSettings, 
  createInvoiceSetting, 
  updateInvoiceSetting, 
  deleteInvoiceSetting, 
  setDefaultInvoiceSetting,
  type InvoiceSetting 
} from '@/api/invoice'

const list = ref<InvoiceSetting[]>([])
const loading = ref(false)
const showEditModal = ref(false)
const editingItem = ref<InvoiceSetting | null>(null)

const form = reactive({
  titleType: 1,
  invoiceTitle: '',
  taxNumber: '',
  bankName: '',
  bankAccount: '',
  companyAddress: '',
  companyPhone: '',
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getInvoiceSettings()
    list.value = res.data || []
  } catch (e) {
    console.error('获取发票抬头列表失败', e)
  } finally {
    loading.value = false
  }
}

const addSetting = () => {
  editingItem.value = null
  Object.assign(form, {
    titleType: 1,
    invoiceTitle: '',
    taxNumber: '',
    bankName: '',
    bankAccount: '',
    companyAddress: '',
    companyPhone: '',
  })
  showEditModal.value = true
}

const editSetting = (item: InvoiceSetting) => {
  editingItem.value = item
  Object.assign(form, {
    titleType: item.titleType,
    invoiceTitle: item.invoiceTitle,
    taxNumber: item.taxNumber || '',
    bankName: item.bankName || '',
    bankAccount: item.bankAccount || '',
    companyAddress: item.companyAddress || '',
    companyPhone: item.companyPhone || '',
  })
  showEditModal.value = true
}

const closeModal = () => {
  showEditModal.value = false
}

const submitForm = async () => {
  if (!form.invoiceTitle.trim()) {
    uni.showToast({ title: '请输入发票抬头', icon: 'none' })
    return
  }
  if (form.titleType === 2 && !form.taxNumber.trim()) {
    uni.showToast({ title: '请输入税号', icon: 'none' })
    return
  }

  try {
    if (editingItem.value) {
      await updateInvoiceSetting(editingItem.value.settingId, form)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      await createInvoiceSetting(form)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    closeModal()
    fetchList()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const setDefault = async (item: InvoiceSetting) => {
  try {
    await setDefaultInvoiceSetting(item.settingId)
    uni.showToast({ title: '设置成功', icon: 'success' })
    fetchList()
  } catch (e) {
    uni.showToast({ title: '设置失败', icon: 'none' })
  }
}

const deleteSetting = (item: InvoiceSetting) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该发票抬头吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteInvoiceSetting(item.settingId)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchList()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f5f7fa 100%);
  padding-bottom: 140rpx;
}

.settings-list {
  padding: 24rpx 32rpx;
}

.setting-card {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.card-main {
  padding: 28rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.type-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  background: #e3f2fd;
  color: #2196f3;
  border-radius: 20rpx;
}

.title-text {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.default-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 20rpx;
}

.info-row {
  display: flex;
  margin-bottom: 8rpx;
  
  .label {
    width: 120rpx;
    font-size: 26rpx;
    color: #999;
  }
  
  .value {
    flex: 1;
    font-size: 26rpx;
    color: #666;
  }
}

.card-actions {
  display: flex;
  border-top: 1rpx solid #f5f5f5;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  font-size: 26rpx;
  color: #667eea;
  
  &.edit {
    border-left: 1rpx solid #f5f5f5;
    border-right: 1rpx solid #f5f5f5;
  }
  
  &.delete {
    color: #f44336;
  }
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

.btn-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 48rpx;
  
  .icon {
    font-size: 36rpx;
    font-weight: 300;
  }
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
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.empty-tip {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
  margin-top: 16rpx;
}

// Modal styles
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.close-btn {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  flex: 1;
  padding: 32rpx;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  
  .required {
    color: #f44336;
  }
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  box-sizing: border-box;
}

.type-selector {
  display: flex;
  gap: 24rpx;
}

.type-option {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 28rpx;
  color: #666;
  background: #f5f7fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  
  &.active {
    color: #667eea;
    background: #f0f4ff;
    border-color: #667eea;
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f5f5f5;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  font-size: 30rpx;
  border-radius: 48rpx;
}

.btn-cancel {
  color: #666;
  background: #f5f7fa;
}

.btn-confirm {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
