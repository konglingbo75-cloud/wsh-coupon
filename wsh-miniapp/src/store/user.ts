/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, clearToken } from '@/api/request'
import { doWechatLogin, logout as apiLogout, type LoginResult } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null)
  const userId = ref<number | null>(null)
  const openid = ref<string | null>(null)
  const nickname = ref<string>('')
  const avatarUrl = ref<string>('')
  const phone = ref<string>('')
  const role = ref<number>(0) // 0消费者 1商户管理 2商户员工

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isMerchant = computed(() => role.value >= 1)
  const isAdmin = computed(() => role.value === 1)

  /**
   * 检查登录状态
   */
  function checkLoginStatus() {
    const savedToken = getToken()
    if (savedToken) {
      token.value = savedToken
      // 可以在这里请求用户信息刷新
    }
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(info: LoginResult) {
    token.value = info.token
    userId.value = info.userId
    openid.value = info.openid
    nickname.value = info.nickname || ''
    avatarUrl.value = info.avatarUrl || ''
    phone.value = info.phone || ''
    role.value = info.role
  }

  /**
   * 微信登录
   */
  async function login(): Promise<LoginResult> {
    const result = await doWechatLogin()
    setUserInfo(result)
    return result
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = null
    userId.value = null
    openid.value = null
    nickname.value = ''
    avatarUrl.value = ''
    phone.value = ''
    role.value = 0
    apiLogout()
  }

  /**
   * 更新手机号
   */
  function updatePhone(newPhone: string) {
    phone.value = newPhone
  }

  return {
    // 状态
    token,
    userId,
    openid,
    nickname,
    avatarUrl,
    phone,
    role,
    // 计算属性
    isLoggedIn,
    isMerchant,
    isAdmin,
    // 方法
    checkLoginStatus,
    setUserInfo,
    login,
    logout,
    updatePhone
  }
})
