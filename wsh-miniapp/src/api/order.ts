/**
 * 订单相关 API
 */
import { get, post } from './request'

export interface OrderItem {
  orderId: number
  orderNo: string
  userId: number
  merchantId: number
  merchantName: string
  merchantLogo: string
  activityId: number
  activityName: string
  activityCover: string
  orderType: number
  orderTypeName: string
  orderAmount: number
  payAmount: number
  status: number
  statusName: string
  payTime: string
  transactionId: string
  isDormancyAwake: boolean
  createdAt: string
  voucherInfo?: {
    voucherId: number
    voucherCode: string
    voucherType: number
    voucherValue: number
    status: number
    validStartTime: string
    validEndTime: string
  }
}

export interface OrderListResponse {
  orders: OrderItem[]
  total: number
  page: number
  pageSize: number
}

export interface PaymentParams {
  orderNo: string
  prepayId: string
  timeStamp: string
  nonceStr: string
  signType: string
  paySign: string
  packageValue: string
}

/**
 * 创建订单
 */
export function createOrder(activityId: number, quantity: number = 1): Promise<OrderItem> {
  return post<OrderItem>('/v1/orders', { activityId, quantity })
}

/**
 * 发起支付
 */
export function requestPayment(orderId: number): Promise<PaymentParams> {
  return post<PaymentParams>(`/v1/orders/${orderId}/pay`)
}

/**
 * 获取订单列表
 */
export function getOrders(status?: number, page: number = 1, pageSize: number = 20): Promise<OrderListResponse> {
  return get<OrderListResponse>('/v1/orders', { status, page, pageSize })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderId: number): Promise<OrderItem> {
  return get<OrderItem>(`/v1/orders/${orderId}`)
}

/**
 * 取消订单
 */
export function cancelOrder(orderId: number): Promise<void> {
  return post(`/v1/orders/${orderId}/cancel`)
}

/**
 * 调起微信支付
 */
export function doWechatPay(params: PaymentParams): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: params.packageValue,
      signType: params.signType as 'MD5' | 'HMAC-SHA256' | 'RSA',
      paySign: params.paySign,
      success: () => resolve(),
      fail: (err) => reject(err)
    })
  })
}
