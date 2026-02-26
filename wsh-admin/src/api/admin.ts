import { post, get, put, del } from './request'

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

export interface PackagePurchaseItem {
  purchaseId: number
  merchantId: number
  merchantName: string
  packageName: string
  pricePaid: number
  payStatus: number
  payTime: string
  validStartDate: string
  validEndDate: string
  createdAt: string
}

export function getPackagePurchases(params: PageParams) {
  return get<PageResult<PackagePurchaseItem>>('/v1/admin/billing/packages', params)
}

export interface DepositItem {
  depositId: number
  merchantId: number
  merchantName: string
  depositAmount: number
  payStatus: number
  payTime: string
  refundTime: string
  refundReason: string
  createdAt: string
}

export function getDeposits(params: PageParams) {
  return get<PageResult<DepositItem>>('/v1/admin/billing/deposits', params)
}

export interface ServiceFeeSummaryItem {
  summaryId: number
  merchantId: number
  merchantName: string
  yearMonth: string
  orderCount: number
  totalAmount: number
  serviceFee: number
  deductStatus: number
  deductTime: string
  createdAt: string
}

export function getServiceFeeSummaries(params: PageParams) {
  return get<PageResult<ServiceFeeSummaryItem>>('/v1/admin/billing/service-fees', params)
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

// ========== 套餐管理 ==========

export interface PackageManageItem {
  packageId: number
  packageName: string
  packageType: number
  price: number
  durationMonths: number
  serviceFeeRate: number
  features: string
  status: number
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface PackageCreateParams {
  packageName: string
  packageType: number
  price: number
  durationMonths: number
  serviceFeeRate: number
  features?: string
  sortOrder?: number
}

export interface PackageUpdateParams {
  packageName?: string
  packageType?: number
  price?: number
  durationMonths?: number
  serviceFeeRate?: number
  features?: string
  sortOrder?: number
}

export function getPackageManage(params: PageParams) {
  return get<PageResult<PackageManageItem>>('/v1/admin/billing/packages/manage', params)
}

export function createPackage(data: PackageCreateParams) {
  return post<number>('/v1/admin/billing/packages/manage', data)
}

export function updatePackage(packageId: number, data: PackageUpdateParams) {
  return put<void>(`/v1/admin/billing/packages/manage/${packageId}`, data)
}

export function updatePackageStatus(packageId: number, status: number) {
  return put<void>(`/v1/admin/billing/packages/manage/${packageId}/status`, { status })
}

// ========== 入驻费套餐管理 ==========

export interface OnboardingPlanManageItem {
  planId: number
  planName: string
  planType: number
  feeAmount: number
  durationMonths: number
  description: string
  status: number
  createdAt: string
}

export interface OnboardingPlanCreateParams {
  planName: string
  planType: number
  feeAmount: number
  durationMonths: number
  description?: string
}

export interface OnboardingPlanUpdateParams {
  planName?: string
  planType?: number
  feeAmount?: number
  durationMonths?: number
  description?: string
}

export function getOnboardingPlanManage(params: PageParams) {
  return get<PageResult<OnboardingPlanManageItem>>('/v1/admin/billing/onboarding-plans/manage', params)
}

export function createOnboardingPlan(data: OnboardingPlanCreateParams) {
  return post<number>('/v1/admin/billing/onboarding-plans/manage', data)
}

export function updateOnboardingPlan(planId: number, data: OnboardingPlanUpdateParams) {
  return put<void>(`/v1/admin/billing/onboarding-plans/manage/${planId}`, data)
}

export function updateOnboardingPlanStatus(planId: number, status: number) {
  return put<void>(`/v1/admin/billing/onboarding-plans/manage/${planId}/status`, { status })
}

// ========== 保证金退款审核 ==========

export function handleDepositRefund(depositId: number, action: 'approve' | 'reject', reason?: string) {
  return put<void>(`/v1/admin/billing/deposits/${depositId}/refund`, { action, reason })
}

// ========== 服务费手动调整 ==========

export function manualDeductServiceFee(summaryId: number) {
  return post<void>(`/v1/admin/billing/service-fees/${summaryId}/deduct`)
}

export function adjustServiceFeeStatus(summaryId: number, newStatus: number, reason: string) {
  return put<void>(`/v1/admin/billing/service-fees/${summaryId}/status`, { newStatus, reason })
}

// ========== AI模型管理 ==========

export interface AiModelConfigItem {
  configId: number
  providerCode: string
  providerName: string
  modelName: string
  apiEndpoint: string
  isDefault: number
  status: number
  inputPrice: number
  outputPrice: number
  maxTokens: number
  temperature: number
  createdAt: string
  updatedAt: string
}

export interface AiModelConfigParams {
  providerCode: string
  providerName: string
  modelName: string
  apiEndpoint?: string
  apiKey?: string
  isDefault?: number
  status: number
  inputPrice?: number
  outputPrice?: number
  maxTokens?: number
  temperature?: number
}

export function getAiModelConfigs(params: PageParams) {
  return get<PageResult<AiModelConfigItem>>('/v1/admin/ai/models', params)
}

export function createAiModelConfig(data: AiModelConfigParams) {
  return post<AiModelConfigItem>('/v1/admin/ai/models', data)
}

export function updateAiModelConfig(configId: number, data: AiModelConfigParams) {
  return put<AiModelConfigItem>(`/v1/admin/ai/models/${configId}`, data)
}

export function deleteAiModelConfig(configId: number) {
  return del(`/v1/admin/ai/models/${configId}`)
}

export function setDefaultAiModel(configId: number) {
  return put<void>(`/v1/admin/ai/models/${configId}/default`)
}

export interface AiUsageDailyItem {
  dailyId: number
  statDate: string
  modelConfigId: number
  modelName: string
  providerName: string
  callCount: number
  totalInputTokens: number
  totalOutputTokens: number
  totalCost: number
}

export interface AiUsageSummary {
  totalCallCount: number
  totalInputTokens: number
  totalOutputTokens: number
  totalCost: number
  todayCallCount: number
  todayCost: number
  monthCallCount: number
  monthCost: number
}

export function getAiUsageDaily(params: PageParams & { startDate?: string; endDate?: string }) {
  return get<PageResult<AiUsageDailyItem>>('/v1/admin/ai/usage/daily', params)
}

export function getAiUsageSummary() {
  return get<AiUsageSummary>('/v1/admin/ai/usage/summary')
}

// ========== 城市管理 ==========

export interface CityItem {
  cityId: number
  cityCode: string
  cityName: string
  provinceName: string
  pinyin: string
  level: number
  longitude: number
  latitude: number
  merchantCount: number
  activityCount: number
  status: number
  sortOrder: number
  openDate: string
  createdAt: string
  updatedAt: string
}

export interface CityCreateParams {
  cityCode: string
  cityName: string
  provinceName?: string
  pinyin?: string
  level: number
  longitude?: number
  latitude?: number
  sortOrder?: number
  status?: number
}

export interface CityUpdateParams {
  cityCode?: string
  cityName?: string
  provinceName?: string
  pinyin?: string
  level?: number
  longitude?: number
  latitude?: number
  sortOrder?: number
}

export function getCities(params: PageParams) {
  return get<PageResult<CityItem>>('/v1/admin/cities', params)
}

export function getCityDetail(cityId: number) {
  return get<CityItem>(`/v1/admin/cities/${cityId}`)
}

export function createCity(data: CityCreateParams) {
  return post<CityItem>('/v1/admin/cities', data)
}

export function updateCity(cityId: number, data: CityUpdateParams) {
  return put<CityItem>(`/v1/admin/cities/${cityId}`, data)
}

export function updateCityStatus(cityId: number, status: number) {
  return put<void>(`/v1/admin/cities/${cityId}/status`, { status })
}

// ========== 拼团管理 ==========

export interface GroupOrderItem {
  groupOrderId: number
  groupNo: string
  activityId: number
  activityName: string
  merchantName: string
  initiatorUserId: number
  initiatorNickname: string
  requiredMembers: number
  currentMembers: number
  status: number
  statusName: string
  expireTime: string
  completeTime: string | null
  createdAt: string
}

export interface GroupParticipantItem {
  participantId: number
  userId: number
  nickname: string
  avatarUrl: string
  isInitiator: boolean
  orderId: number | null
  orderStatus: number | null
  joinTime: string
}

export interface GroupOrderDetail extends GroupOrderItem {
  participants: GroupParticipantItem[]
}

export function getGroupOrders(params: PageParams & { activityId?: number }) {
  return get<PageResult<GroupOrderItem>>('/v1/admin/group-orders', params)
}

export function getGroupOrderDetail(groupOrderId: number) {
  return get<GroupOrderDetail>(`/v1/admin/group-orders/${groupOrderId}`)
}
