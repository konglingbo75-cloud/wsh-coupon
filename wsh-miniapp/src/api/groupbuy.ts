/**
 * 拼团服务 API
 */
import { get, post } from './request'

/**
 * 参与者信息
 */
export interface ParticipantItem {
  userId: number
  nickname: string
  avatarUrl: string
  isInitiator: boolean
  joinTime: string
}

/**
 * 拼团详情
 */
export interface GroupDetail {
  groupOrderId: number
  groupNo: string
  activityId: number
  activityName: string
  coverImage: string
  merchantId: number
  merchantName: string
  groupPrice: number
  originalPrice: number
  requiredMembers: number
  currentMembers: number
  remainingMembers: number
  status: number
  statusName: string
  expireTime: string
  remainingSeconds: number
  completeTime: string | null
  initiatorUserId: number
  initiatorNickname: string
  initiatorAvatar: string
  isInitiator: boolean
  hasJoined: boolean
  participants: ParticipantItem[]
  createdAt: string
}

/**
 * 拼团列表项
 */
export interface GroupItem {
  groupOrderId: number
  groupNo: string
  activityId: number
  activityName: string
  coverImage: string
  merchantName: string
  groupPrice: number
  requiredMembers: number
  currentMembers: number
  status: number
  statusName: string
  isInitiator: boolean
  expireTime: string
  remainingSeconds: number
  createdAt: string
}

/**
 * 拼团列表响应
 */
export interface GroupListResponse {
  groups: GroupItem[]
  total: number
  page: number
  pageSize: number
}

/**
 * 支付响应
 */
export interface PaymentResponse {
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
}

/**
 * 发起拼团
 */
export function initiateGroup(activityId: number): Promise<GroupDetail> {
  return post<GroupDetail>('/v1/group-buy/initiate', { activityId })
}

/**
 * 参与拼团
 */
export function joinGroup(groupOrderId: number): Promise<GroupDetail> {
  return post<GroupDetail>('/v1/group-buy/join', { groupOrderId })
}

/**
 * 获取拼团详情
 */
export function getGroupDetail(groupOrderId: number): Promise<GroupDetail> {
  return get<GroupDetail>(`/v1/group-buy/${groupOrderId}`)
}

/**
 * 获取活动的可参与拼团列表
 */
export function getActivityGroups(activityId: number): Promise<GroupDetail[]> {
  return get<GroupDetail[]>(`/v1/group-buy/activity/${activityId}`)
}

/**
 * 获取我的拼团列表
 */
export function getMyGroups(params: { status?: number; page?: number; pageSize?: number }): Promise<GroupListResponse> {
  return get<GroupListResponse>('/v1/group-buy/my', params)
}

/**
 * 发起拼团支付
 */
export function requestGroupPayment(groupOrderId: number): Promise<PaymentResponse> {
  return post<PaymentResponse>(`/v1/group-buy/${groupOrderId}/pay`)
}

/**
 * 取消拼团
 */
export function cancelGroup(groupOrderId: number): Promise<void> {
  return post<void>(`/v1/group-buy/${groupOrderId}/cancel`)
}
