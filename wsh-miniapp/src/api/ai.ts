import request from './request'

// AI助手相关接口

export interface ConversationItem {
  conversationId: number
  title: string
  messageCount: number
  totalTokens: number
  totalCost: number
  createdAt: string
  updatedAt: string
}

export interface MessageItem {
  messageId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  inputTokens: number
  outputTokens: number
  createdAt: string
}

export interface ConversationDetail extends ConversationItem {
  messages: MessageItem[]
}

export interface ChatResponse {
  messageId: number
  conversationId: number
  role: string
  content: string
  inputTokens: number
  outputTokens: number
  cost: number
  createdAt: string
}

// 创建新对话
export function createConversation() {
  return request.post<ConversationItem>('/v1/ai/conversations')
}

// 获取对话列表
export function getConversationList(params: { page?: number; size?: number }) {
  return request.get<{ list: ConversationItem[]; total: number }>('/v1/ai/conversations', params)
}

// 获取对话详情
export function getConversationDetail(conversationId: number) {
  return request.get<ConversationDetail>(`/v1/ai/conversations/${conversationId}`)
}

// 删除对话
export function deleteConversation(conversationId: number) {
  return request.delete(`/v1/ai/conversations/${conversationId}`)
}

// 发送消息
export function sendMessage(conversationId: number, content: string) {
  return request.post<ChatResponse>(`/v1/ai/conversations/${conversationId}/messages`, { content })
}
