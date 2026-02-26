/**
 * API 请求封装
 */

// API 基础地址
// #ifdef H5
const BASE_URL = '/api'
// #endif
// #ifndef H5
// 根据构建模式自动切换：
// - 开发环境 (npm run dev): 使用本地后端
// - 生产环境 (npm run build): 使用生产服务器
let BASE_URL: string
if (process.env.NODE_ENV === 'production') {
  BASE_URL = 'https://your-domain.com'  // 部署前替换为实际域名
} else {
  BASE_URL = 'http://localhost:8080'
}
// #endif

// 请求超时时间
const TIMEOUT = 30000

// Token 存储 key
const TOKEN_KEY = 'wsh_token'

/**
 * 获取 Token
 */
export function getToken(): string | null {
  return uni.getStorageSync(TOKEN_KEY) || null
}

/**
 * 设置 Token
 */
export function setToken(token: string): void {
  uni.setStorageSync(TOKEN_KEY, token)
}

/**
 * 清除 Token
 */
export function clearToken(): void {
  uni.removeStorageSync(TOKEN_KEY)
}

/**
 * 统一响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 请求配置
 */
interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
}

/**
 * 发起请求
 */
export function request<T = any>(options: RequestOptions): Promise<T> {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    showLoading = false,
    showError = true
  } = options

  return new Promise((resolve, reject) => {
    // 显示 loading
    if (showLoading) {
      uni.showLoading({ title: '加载中...' })
    }

    // 添加 Token
    const token = getToken()
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    header['Content-Type'] = header['Content-Type'] || 'application/json'

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      timeout: TIMEOUT,
      success: (res) => {
        if (showLoading) {
          uni.hideLoading()
        }

        const response = res.data as ApiResponse<T>

        // 业务成功
        if (response.code === 200) {
          resolve(response.data)
          return
        }

        // 未登录
        if (response.code === 401) {
          clearToken()
          uni.reLaunch({ url: '/pages/login/index' })
          reject(new Error(response.message || '请先登录'))
          return
        }

        // 其他错误
        if (showError) {
          uni.showToast({
            title: response.message || '请求失败',
            icon: 'none',
            duration: 2000
          })
        }
        reject(new Error(response.message))
      },
      fail: (err) => {
        if (showLoading) {
          uni.hideLoading()
        }
        
        if (showError) {
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none',
            duration: 2000
          })
        }
        reject(err)
      }
    })
  })
}

/**
 * GET 请求
 */
export function get<T = any>(url: string, params?: Record<string, any>, options?: Partial<RequestOptions>): Promise<T> {
  // 拼接查询参数
  if (params) {
    const queryString = Object.entries(params)
      .filter(([_, v]) => v !== undefined && v !== null)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')
    if (queryString) {
      url += (url.includes('?') ? '&' : '?') + queryString
    }
  }
  return request<T>({ url, method: 'GET', ...options })
}

/**
 * POST 请求
 */
export function post<T = any>(url: string, data?: any, options?: Partial<RequestOptions>): Promise<T> {
  return request<T>({ url, method: 'POST', data, ...options })
}

/**
 * PUT 请求
 */
export function put<T = any>(url: string, data?: any, options?: Partial<RequestOptions>): Promise<T> {
  return request<T>({ url, method: 'PUT', data, ...options })
}

/**
 * DELETE 请求
 */
export function del<T = any>(url: string, options?: Partial<RequestOptions>): Promise<T> {
  return request<T>({ url, method: 'DELETE', ...options })
}

export default {
  get,
  post,
  put,
  del,
  getToken,
  setToken,
  clearToken
}
