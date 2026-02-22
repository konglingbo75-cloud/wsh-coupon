/**
 * 应用全局状态
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 当前位置
  const currentCity = ref<string>('深圳')
  const currentLocation = ref<{ latitude: number; longitude: number } | null>(null)

  // 系统信息
  const systemInfo = ref<UniApp.GetSystemInfoResult | null>(null)

  /**
   * 获取位置
   */
  async function getLocation(): Promise<{ latitude: number; longitude: number }> {
    return new Promise((resolve, reject) => {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          currentLocation.value = {
            latitude: res.latitude,
            longitude: res.longitude
          }
          resolve(currentLocation.value)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }

  /**
   * 设置城市
   */
  function setCity(city: string) {
    currentCity.value = city
  }

  /**
   * 获取系统信息
   */
  function getSystemInfo() {
    if (!systemInfo.value) {
      systemInfo.value = uni.getSystemInfoSync()
    }
    return systemInfo.value
  }

  return {
    currentCity,
    currentLocation,
    systemInfo,
    getLocation,
    setCity,
    getSystemInfo
  }
})
