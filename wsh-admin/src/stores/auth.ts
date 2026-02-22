import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminId = ref<number>(Number(localStorage.getItem('admin_id')) || 0)
  const username = ref(localStorage.getItem('admin_username') || '')
  const realName = ref(localStorage.getItem('admin_realName') || '')
  const role = ref<number>(Number(localStorage.getItem('admin_role')) || 0)

  function setAuth(data: { token: string; adminId: number; username: string; realName: string; role: number }) {
    token.value = data.token
    adminId.value = data.adminId
    username.value = data.username
    realName.value = data.realName
    role.value = data.role
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_id', String(data.adminId))
    localStorage.setItem('admin_username', data.username)
    localStorage.setItem('admin_realName', data.realName)
    localStorage.setItem('admin_role', String(data.role))
  }

  function logout() {
    token.value = ''
    adminId.value = 0
    username.value = ''
    realName.value = ''
    role.value = 0
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_id')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_realName')
    localStorage.removeItem('admin_role')
  }

  const isLoggedIn = () => !!token.value

  return { token, adminId, username, realName, role, setAuth, logout, isLoggedIn }
})
