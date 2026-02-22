/**
 * 认证相关 API
 */
import { post, setToken, clearToken } from './request'

export interface LoginResult {
  token: string
  userId: number
  openid: string
  nickname?: string
  avatarUrl?: string
  phone?: string
  role: number
  isNewUser: boolean
}

/**
 * 微信登录
 */
export function wechatLogin(code: string): Promise<LoginResult> {
  return post<LoginResult>('/v1/auth/wechat/login', { code })
}

/**
 * 绑定手机号
 */
export function bindPhone(encryptedData: string, iv: string): Promise<{ phone: string }> {
  return post('/v1/auth/bindPhone', { encryptedData, iv })
}

/**
 * 获取手机号（一键登录）
 */
export function getPhoneNumber(code: string): Promise<{ phone: string }> {
  return post('/v1/auth/phone', { code })
}

/**
 * 执行微信登录流程
 */
export function doWechatLogin(): Promise<LoginResult> {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: async (loginRes) => {
        if (loginRes.code) {
          try {
            const result = await wechatLogin(loginRes.code)
            setToken(result.token)
            resolve(result)
          } catch (err) {
            reject(err)
          }
        } else {
          reject(new Error('获取登录凭证失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 退出登录
 */
export function logout(): void {
  clearToken()
  uni.reLaunch({ url: '/pages/login/index' })
}
