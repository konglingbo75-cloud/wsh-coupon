/**
 * 权益相关 API
 */
import { get, put } from './request'

export interface EquitySummary {
  totalPointsValue: number
  totalBalance: number
  totalVoucherValue: number
  expiringPointsValue: number
  expiringVoucherCount: number
  merchantCount: number
  lastUpdated: string
}

export interface ExpiringEquity {
  id: number
  merchantId: number
  merchantName: string
  merchantLogo: string
  equityType: 'points' | 'balance' | 'voucher'
  equityTypeName: string
  equityValue: number
  expireDate: string
  remainingDays: number
}

export interface NotificationSettings {
  pointsExpireNotify: boolean
  balanceNotify: boolean
  voucherExpireNotify: boolean
  activityNotify: boolean
}

/**
 * 获取权益资产总览
 */
export function getEquitySummary(): Promise<EquitySummary> {
  return get<EquitySummary>('/v1/equity/summary')
}

/**
 * 获取即将过期的权益列表
 */
export function getExpiringEquities(): Promise<ExpiringEquity[]> {
  return get<ExpiringEquity[]>('/v1/equity/expiring')
}

/**
 * 获取权益提醒消息列表
 */
export function getEquityMessages(page: number = 1, pageSize: number = 20): Promise<{
  list: any[]
  total: number
}> {
  return get('/v1/equity/messages', { page, pageSize })
}

/**
 * 获取提醒设置
 */
export function getNotificationSettings(): Promise<NotificationSettings> {
  return get<NotificationSettings>('/v1/equity/notification-settings')
}

/**
 * 更新提醒设置
 */
export function updateNotificationSettings(settings: Partial<NotificationSettings>): Promise<void> {
  return put('/v1/equity/notification-settings', settings)
}
