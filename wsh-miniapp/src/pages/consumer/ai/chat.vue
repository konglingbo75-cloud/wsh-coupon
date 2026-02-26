<template>
  <view class="ai-chat-page">
    <!-- 头部 -->
    <view class="chat-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-center">
        <view class="ai-avatar">
          <text class="ai-icon">🤖</text>
        </view>
        <view class="header-info">
          <text class="title">AI智能助手</text>
          <text class="subtitle">为您提供权益咨询服务</text>
        </view>
      </view>
      <view class="header-right" @click="showHistory">
        <text class="history-icon">📋</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view 
      class="message-list"
      scroll-y
      :scroll-into-view="scrollToId"
      :scroll-with-animation="true"
    >
      <!-- 欢迎消息 -->
      <view v-if="messages.length === 0" class="welcome-section">
        <view class="welcome-card">
          <view class="welcome-avatar">🤖</view>
          <text class="welcome-title">你好！我是微生活券吧AI助手</text>
          <text class="welcome-desc">我可以帮您：</text>
          <view class="feature-list">
            <view class="feature-item" @click="quickAsk('我有多少积分快过期了？')">
              <text class="feature-icon">💰</text>
              <text class="feature-text">查询权益余额和过期时间</text>
            </view>
            <view class="feature-item" @click="quickAsk('附近有什么优惠活动？')">
              <text class="feature-icon">🎁</text>
              <text class="feature-text">推荐附近优惠活动</text>
            </view>
            <view class="feature-item" @click="quickAsk('我的订单是什么状态？')">
              <text class="feature-icon">📦</text>
              <text class="feature-text">查询订单状态</text>
            </view>
            <view class="feature-item" @click="quickAsk('怎么使用优惠券？')">
              <text class="feature-icon">❓</text>
              <text class="feature-text">解答平台使用问题</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view v-for="(msg, index) in messages" :key="msg.messageId || index" :id="'msg-' + index">
        <view :class="['message-item', msg.role]">
          <view v-if="msg.role === 'assistant'" class="avatar">🤖</view>
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
            <text class="message-time">{{ formatTime(msg.createdAt) }}</text>
          </view>
          <view v-if="msg.role === 'user'" class="avatar user-avatar">👤</view>
        </view>
      </view>

      <!-- 加载中提示 -->
      <view v-if="isLoading" class="message-item assistant">
        <view class="avatar">🤖</view>
        <view class="message-bubble typing">
          <view class="typing-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>

      <view id="scroll-bottom" style="height: 20rpx;"></view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrapper">
        <input 
          v-model="inputText"
          class="chat-input"
          placeholder="输入您的问题..."
          :disabled="isLoading"
          confirm-type="send"
          @confirm="sendMessage"
        />
        <view 
          :class="['send-btn', { disabled: !inputText.trim() || isLoading }]"
          @click="sendMessage"
        >
          <text class="send-icon">↑</text>
        </view>
      </view>
      <view class="input-tip">AI回答仅供参考，具体以实际为准</view>
    </view>

    <!-- 历史对话弹窗 -->
    <view v-if="showHistoryModal" class="modal-mask" @click="showHistoryModal = false">
      <view class="history-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">历史对话</text>
          <view class="close-btn" @click="showHistoryModal = false">×</view>
        </view>
        <scroll-view class="history-list" scroll-y>
          <view v-if="historyList.length === 0" class="empty-history">
            <text>暂无历史对话</text>
          </view>
          <view 
            v-for="item in historyList" 
            :key="item.conversationId"
            class="history-item"
            @click="loadConversation(item.conversationId)"
          >
            <text class="history-title">{{ item.title }}</text>
            <text class="history-time">{{ formatTime(item.createdAt) }}</text>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <view class="btn-new" @click="startNewConversation">开始新对话</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { 
  createConversation, 
  getConversationList, 
  getConversationDetail, 
  sendMessage as sendChatMessage,
  type MessageItem,
  type ConversationItem
} from '@/api/ai'

interface LocalMessage {
  messageId?: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt?: string
}

const messages = ref<LocalMessage[]>([])
const inputText = ref('')
const isLoading = ref(false)
const scrollToId = ref('scroll-bottom')
const currentConversationId = ref<number | null>(null)
const showHistoryModal = ref(false)
const historyList = ref<ConversationItem[]>([])

const scrollToBottom = () => {
  nextTick(() => {
    scrollToId.value = ''
    setTimeout(() => {
      scrollToId.value = 'scroll-bottom'
    }, 100)
  })
}

const initConversation = async () => {
  try {
    const res = await createConversation()
    currentConversationId.value = res.data.conversationId
  } catch (e) {
    console.error('创建对话失败', e)
    // 使用本地模式
    currentConversationId.value = Date.now()
  }
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  })
  inputText.value = ''
  scrollToBottom()

  // 确保有对话ID
  if (!currentConversationId.value) {
    await initConversation()
  }

  isLoading.value = true
  try {
    const res = await sendChatMessage(currentConversationId.value!, content)
    messages.value.push({
      messageId: res.data.messageId,
      role: 'assistant',
      content: res.data.content,
      createdAt: res.data.createdAt
    })
  } catch (e: any) {
    console.error('发送消息失败', e)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。',
      createdAt: new Date().toISOString()
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const quickAsk = (question: string) => {
  inputText.value = question
  sendMessage()
}

const showHistory = async () => {
  try {
    const res = await getConversationList({ page: 1, size: 20 })
    historyList.value = res.data?.list || []
    showHistoryModal.value = true
  } catch (e) {
    uni.showToast({ title: '获取历史失败', icon: 'none' })
  }
}

const loadConversation = async (conversationId: number) => {
  try {
    const res = await getConversationDetail(conversationId)
    currentConversationId.value = conversationId
    messages.value = (res.data.messages || []).map(m => ({
      messageId: m.messageId,
      role: m.role,
      content: m.content,
      createdAt: m.createdAt
    }))
    showHistoryModal.value = false
    scrollToBottom()
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const startNewConversation = async () => {
  messages.value = []
  currentConversationId.value = null
  await initConversation()
  showHistoryModal.value = false
}

const goBack = () => {
  uni.navigateBack()
}

const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

onMounted(() => {
  initConversation()
})
</script>

<style lang="scss" scoped>
.ai-chat-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 60rpx 32rpx 32rpx;
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.3) 0%, transparent 100%);
}

.header-left, .header-right {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 56rpx;
  color: #fff;
}

.history-icon {
  font-size: 40rpx;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.ai-avatar {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.5);
}

.ai-icon {
  font-size: 40rpx;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

.message-list {
  flex: 1;
  padding: 0 32rpx;
  padding-bottom: 200rpx;
}

.welcome-section {
  padding: 40rpx 0;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20rpx);
  border-radius: 32rpx;
  padding: 48rpx;
  text-align: center;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
}

.welcome-avatar {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.welcome-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16rpx;
}

.welcome-desc {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 32rpx;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  transition: all 0.3s;
  
  &:active {
    background: rgba(102, 126, 234, 0.3);
    transform: scale(0.98);
  }
}

.feature-icon {
  font-size: 36rpx;
}

.feature-text {
  font-size: 28rpx;
  color: #fff;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 32rpx;
  
  &.user {
    flex-direction: row-reverse;
  }
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
  
  &.user-avatar {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
}

.message-bubble {
  max-width: 70%;
  padding: 24rpx 28rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 24rpx;
  border-top-left-radius: 8rpx;
  
  .user & {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    border-top-right-radius: 8rpx;
  }
  
  &.typing {
    padding: 28rpx 32rpx;
  }
}

.message-text {
  font-size: 28rpx;
  color: #fff;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  display: block;
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8rpx;
  text-align: right;
}

.typing-dots {
  display: flex;
  gap: 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: typing 1.4s infinite;
  
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.6; }
  30% { transform: translateY(-10rpx); opacity: 1; }
}

.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, transparent 0%, rgba(26, 26, 46, 0.95) 20%);
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 8rpx 8rpx 8rpx 32rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 48rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.chat-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  color: #fff;
  background: transparent;
}

.chat-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.send-btn {
  width: 72rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.disabled {
    opacity: 0.5;
  }
}

.send-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: bold;
}

.input-tip {
  text-align: center;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 16rpx;
}

// Modal styles
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.history-modal {
  width: 100%;
  max-height: 70vh;
  background: #1a1a2e;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.close-btn {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1;
}

.history-list {
  flex: 1;
  padding: 16rpx 32rpx;
}

.empty-history {
  text-align: center;
  padding: 60rpx;
  color: rgba(255, 255, 255, 0.5);
  font-size: 28rpx;
}

.history-item {
  padding: 28rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.history-title {
  display: block;
  font-size: 28rpx;
  color: #fff;
  margin-bottom: 8rpx;
}

.history-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.4);
}

.modal-footer {
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid rgba(255, 255, 255, 0.1);
}

.btn-new {
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 30rpx;
  border-radius: 48rpx;
}
</style>
