/**
 * 会员匹配相关 API
 */
import { get, post } from './request'

export interface MemberMatchResponse {
  matchedCount: number
  matchedMerchantIds: number[]
  activityCount: number
}

export interface MerchantMember {
  snapshotId: number
  userId: number
  merchantId: number
  merchantName: string
  merchantLogoUrl: string | null
  sourceMemberId: string
  memberLevelName: string
  points: number
  pointsValue: number
  pointsExpireDate: string
  balance: number
  consumeCount: number
  totalConsumeAmount: number
  lastConsumeTime: string | null
  dormancyLevel: number
  dormancyDesc: string
  syncTime: string
}

export interface ExclusiveActivity {
  activityId: number
  merchantId: number
  merchantName: string
  activityType: number
  activityName: string
  activityDesc: string
  coverImage: string | null
  startTime: string
  endTime: string
  config: string
  stock: number
  soldCount: number
  targetMemberType: number
  isPublic: number
}

/**
 * 触发会员匹配（用当前用户手机号去所有商户匹配会员身份）
 */
export function triggerMatching(): Promise<MemberMatchResponse> {
  return post<MemberMatchResponse>('/v1/matching/trigger')
}

/**
 * 获取我的会员列表（各商户会员快照）
 */
export function getMemberList(): Promise<MerchantMember[]> {
  return get<MerchantMember[]>('/v1/matching/members')
}

/**
 * 获取专属活动列表（根据会员状态筛选的活动）
 */
export function getExclusiveActivities(): Promise<ExclusiveActivity[]> {
  return get<ExclusiveActivity[]>('/v1/matching/activities')
}
