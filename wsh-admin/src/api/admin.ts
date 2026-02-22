import { post, get } from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  adminId: number
  username: string
  realName: string
  role: number
}

export function adminLogin(data: LoginParams) {
  return post<LoginResult>('/v1/admin/auth/login', data)
}

export interface DashboardStats {
  totalMerchants: number
  pendingMerchants: number
  activeMerchants: number
  frozenMerchants: number
  totalUsers: number
  totalActivities: number
  activeActivities: number
  totalOrders: number
  totalOrderAmount: number
  verifiedVouchers: number
  totalOnboardingFee: number
  totalServiceFee: number
}

export function getDashboardStats() {
  return get<DashboardStats>('/v1/admin/dashboard/stats')
}

export interface PageParams {
  page?: number
  size?: number
  keyword?: string
  status?: number | null
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface MerchantItem {
  merchantId: number
  merchantCode: string
  merchantName: string
  contactName: string
  contactPhone: string
  city: string
  businessCategory: string
  status: number
  profitSharingRate: number
  integrationType: number
  branchCount: number
  activityCount: number
  createdAt: string
}

export interface MerchantDetail {
  merchantId: number
  merchantCode: string
  merchantName: string
  logoUrl: string
  contactName: string
  contactPhone: string
  address: string
  city: string
  longitude: number
  latitude: number
  businessCategory: string
  status: number
  integrationType: number
  profitSharingRate: number
  createdAt: string
  branches: { branchId: number; branchName: string; address: string; status: number }[]
  onboardingFee: { feeId: number; planName: string; feeAmount: number; payStatus: number; payTime: string; validStartDate: string; validEndDate: string } | null
  auditLogs: { logId: number; action: string; prevStatus: number; newStatus: number; reason: string; adminName: string; createdAt: string }[]
}

export function getMerchants(params: PageParams) {
  return get<PageResult<MerchantItem>>('/v1/admin/merchants', params)
}

export function getMerchantDetail(merchantId: number) {
  return get<MerchantDetail>(`/v1/admin/merchants/${merchantId}`)
}

export function auditMerchant(data: { merchantId: number; action: string; reason?: string }) {
  return post('/v1/admin/merchants/audit', data)
}

export function updateMerchantStatus(data: { merchantId: number; action: string; reason?: string }) {
  return post('/v1/admin/merchants/status', data)
}

export interface UserItem {
  userId: number
  nickname: string
  phone: string
  status: number
  merchantCount: number
  totalConsumeAmount: number
  totalConsumeCount: number
  memberLevel: number
  createdAt: string
}

export function getUsers(params: PageParams) {
  return get<PageResult<UserItem>>('/v1/admin/users', params)
}

export interface ActivityItem {
  activityId: number
  merchantId: number
  merchantName: string
  activityType: number
  activityName: string
  status: number
  stock: number
  soldCount: number
  isPublic: number
  startTime: string
  endTime: string
  createdAt: string
}

export function getActivities(params: PageParams) {
  return get<PageResult<ActivityItem>>('/v1/admin/activities', params)
}

export interface BillingItem {
  feeId: number
  merchantId: number
  merchantName: string
  planName: string
  feeAmount: number
  payStatus: number
  payTime: string
  validStartDate: string
  validEndDate: string
  createdAt: string
}

export function getBillings(params: PageParams) {
  return get<PageResult<BillingItem>>('/v1/admin/billing', params)
}

export interface OperationLogItem {
  logId: number
  adminId: number
  adminName: string
  module: string
  action: string
  targetId: string
  detail: string
  ip: string
  createdAt: string
}

export function getOperationLogs(params: PageParams) {
  return get<PageResult<OperationLogItem>>('/v1/admin/logs', params)
}
