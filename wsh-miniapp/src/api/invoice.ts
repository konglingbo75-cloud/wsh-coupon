import request from './request'

// 发票相关接口

export interface InvoiceItem {
  invoiceId: number
  merchantId: number
  merchantName: string
  consumeRecordId: number
  invoiceNo: string
  invoiceCode: string
  invoiceType: number
  invoiceTypeName: string
  invoiceStatus: number
  invoiceStatusName: string
  invoiceAmount: number
  invoiceTitle: string
  taxNumber: string
  invoiceUrl: string
  invoiceDate: string
  requestTime: string
  completeTime: string
  createdAt: string
}

export interface InvoiceSetting {
  settingId: number
  titleType: number
  titleTypeName: string
  invoiceTitle: string
  taxNumber: string
  bankName: string
  bankAccount: string
  companyAddress: string
  companyPhone: string
  isDefault: number
  createdAt: string
}

// 获取发票列表
export function getInvoiceList(params: { status?: number; page?: number; size?: number }) {
  return request.get<{ list: InvoiceItem[]; total: number }>('/v1/invoices', params)
}

// 获取发票详情
export function getInvoiceDetail(invoiceId: number) {
  return request.get<InvoiceItem>(`/v1/invoices/${invoiceId}`)
}

// 申请开票
export function requestInvoice(data: { consumeRecordId: number; invoiceSettingId: number }) {
  return request.post<InvoiceItem>('/v1/invoices/request', data)
}

// 获取发票抬头设置列表
export function getInvoiceSettings() {
  return request.get<InvoiceSetting[]>('/v1/invoice-settings')
}

// 新增发票抬头
export function createInvoiceSetting(data: {
  titleType: number
  invoiceTitle: string
  taxNumber?: string
  bankName?: string
  bankAccount?: string
  companyAddress?: string
  companyPhone?: string
  isDefault?: number
}) {
  return request.post<InvoiceSetting>('/v1/invoice-settings', data)
}

// 更新发票抬头
export function updateInvoiceSetting(settingId: number, data: {
  titleType?: number
  invoiceTitle?: string
  taxNumber?: string
  bankName?: string
  bankAccount?: string
  companyAddress?: string
  companyPhone?: string
}) {
  return request.put<InvoiceSetting>(`/v1/invoice-settings/${settingId}`, data)
}

// 删除发票抬头
export function deleteInvoiceSetting(settingId: number) {
  return request.delete(`/v1/invoice-settings/${settingId}`)
}

// 设为默认抬头
export function setDefaultInvoiceSetting(settingId: number) {
  return request.put(`/v1/invoice-settings/${settingId}/default`)
}
