/**
 * 券码相关 API
 */
import { get } from './request'

export interface VoucherItem {
  voucherId: number
  voucherCode: string
  orderId: number
  merchantId: number
  merchantName: string
  merchantLogo: string
  activityId: number
  activityName: string
  voucherType: number
  voucherTypeName: string
  voucherValue: number
  status: number
  statusName: string
  validStartTime: string
  validEndTime: string
  usedTime?: string
  createdAt: string
  expiringSoon: boolean
  remainingDays: number
}

export interface VoucherListResponse {
  vouchers: VoucherItem[]
  total: number
  availableCount: number
  usedCount: number
  expiredCount: number
}

/**
 * 获取券包列表
 */
export function getVouchers(status?: number): Promise<VoucherListResponse> {
  return get<VoucherListResponse>('/v1/vouchers', { status })
}

/**
 * 获取券码详情
 */
export function getVoucherDetail(voucherId: number): Promise<VoucherItem> {
  return get<VoucherItem>(`/v1/vouchers/${voucherId}`)
}
