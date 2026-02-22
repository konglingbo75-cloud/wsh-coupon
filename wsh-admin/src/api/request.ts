import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.message || '网络请求失败')
    }
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, params?: any): Promise<{ code: number; data: T; message: string }> {
  return service.get(url, { params }) as any
}

export function post<T = any>(url: string, data?: any): Promise<{ code: number; data: T; message: string }> {
  return service.post(url, data) as any
}

export function put<T = any>(url: string, data?: any): Promise<{ code: number; data: T; message: string }> {
  return service.put(url, data) as any
}

export function del<T = any>(url: string): Promise<{ code: number; data: T; message: string }> {
  return service.delete(url) as any
}

export default service
