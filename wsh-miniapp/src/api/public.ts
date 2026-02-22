/**
 * 公共活动 API（无需登录）
 */
import { get } from './request'

export interface PublicActivity {
  activityId: number
  merchantId: number
  merchantName: string
  merchantLogo: string
  activityType: number
  activityTypeName: string
  activityName: string
  activityDesc: string
  coverImage: string
  startTime: string
  endTime: string
  stock: number
  soldCount: number
  config: string
  originalPrice?: number
  sellingPrice?: number
  distance?: number
}

export interface PublicMerchant {
  merchantId: number
  merchantName: string
  logoUrl: string
  address: string
  businessCategory: string
  longitude: number
  latitude: number
  activityCount: number
  distance?: number
}

export interface PublicActivitiesResponse {
  city: string
  voucherActivities: PublicActivity[]
  depositActivities: PublicActivity[]
  pointsActivities: PublicActivity[]
  groupActivities: PublicActivity[]
  typeCount: {
    voucher: number
    deposit: number
    points: number
    group: number
    total: number
  }
}

/**
 * 获取同城公开活动列表
 */
export function getPublicActivities(city: string): Promise<PublicActivitiesResponse> {
  return get<PublicActivitiesResponse>('/v1/public/activities', { city })
}

/**
 * 获取公开活动详情
 */
export function getPublicActivityDetail(activityId: number): Promise<PublicActivity> {
  return get<PublicActivity>(`/v1/public/activities/${activityId}`)
}

/**
 * 获取同城入驻商户列表
 */
export function getPublicMerchants(city: string): Promise<PublicMerchant[]> {
  return get<PublicMerchant[]>('/v1/public/merchants', { city })
}

/**
 * 获取商户公开活动
 */
export function getMerchantPublicActivities(merchantId: number): Promise<PublicActivity[]> {
  return get<PublicActivity[]>(`/v1/public/merchants/${merchantId}/activities`)
}
