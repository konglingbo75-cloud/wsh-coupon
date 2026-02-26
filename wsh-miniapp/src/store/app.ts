/**
 * 应用全局状态
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCityList, locateCity, type CityItem, type CityListResponse } from '@/api/city'

// 本地存储key
const CITY_STORAGE_KEY = 'wsh_selected_city'
const CITY_LIST_CACHE_KEY = 'wsh_city_list_cache'

export const useAppStore = defineStore('app', () => {
  // 当前选中城市
  const currentCity = ref<CityItem | null>(null)
  // 当前城市名（兼容旧代码）
  const currentCityName = computed(() => currentCity.value?.cityName || '深圳')
  // 当前位置
  const currentLocation = ref<{ latitude: number; longitude: number } | null>(null)
  // 城市列表
  const cityList = ref<CityListResponse | null>(null)
  // 城市选择器是否显示
  const showCitySelector = ref(false)
  // 系统信息
  const systemInfo = ref<UniApp.GetSystemInfoResult | null>(null)
  // 是否正在定位
  const isLocating = ref(false)

  /**
   * 初始化城市信息
   */
  async function initCity() {
    // 从本地存储恢复选中的城市
    const savedCity = uni.getStorageSync(CITY_STORAGE_KEY)
    if (savedCity) {
      try {
        currentCity.value = JSON.parse(savedCity)
      } catch (e) {
        console.error('解析城市缓存失败', e)
      }
    }

    // 如果没有选中城市，尝试GPS定位
    if (!currentCity.value) {
      await autoLocate()
    }
  }

  /**
   * 自动定位城市
   */
  async function autoLocate() {
    try {
      isLocating.value = true
      const location = await getLocation()
      const city = await locateCity(location.longitude, location.latitude)
      setCurrentCity(city)
    } catch (err) {
      console.error('自动定位失败', err)
      // 定位失败时设置默认城市
      if (!currentCity.value) {
        setDefaultCity()
      }
    } finally {
      isLocating.value = false
    }
  }

  /**
   * 设置默认城市
   */
  function setDefaultCity() {
    currentCity.value = {
      cityId: 1,
      cityCode: '440300',
      cityName: '深圳',
      provinceName: '广东省',
      pinyin: 'shenzhen',
      level: 1,
      longitude: 114.057865,
      latitude: 22.543099,
      merchantCount: 0,
      activityCount: 0,
      status: 1,
      sortOrder: 1,
      openDate: ''
    }
  }

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
   * 设置当前城市
   */
  function setCurrentCity(city: CityItem) {
    currentCity.value = city
    uni.setStorageSync(CITY_STORAGE_KEY, JSON.stringify(city))
  }

  /**
   * 设置城市（兼容旧方法，通过城市名设置）
   */
  function setCity(cityName: string) {
    if (cityList.value) {
      // 从热门城市中查找
      const hotCity = cityList.value.hotCities.find(c => c.cityName === cityName)
      if (hotCity) {
        setCurrentCity(hotCity)
        return
      }
      // 从所有城市中查找
      for (const cities of Object.values(cityList.value.allCities)) {
        const city = cities.find(c => c.cityName === cityName)
        if (city) {
          setCurrentCity(city)
          return
        }
      }
    }
    // 没找到，只更新名称
    if (currentCity.value) {
      currentCity.value.cityName = cityName
    } else {
      setDefaultCity()
      if (currentCity.value) {
        currentCity.value.cityName = cityName
      }
    }
  }

  /**
   * 加载城市列表
   */
  async function loadCityList(): Promise<CityListResponse> {
    // 检查缓存
    if (cityList.value) {
      return cityList.value
    }

    // 尝试从本地缓存读取
    const cached = uni.getStorageSync(CITY_LIST_CACHE_KEY)
    if (cached) {
      try {
        const data = JSON.parse(cached)
        // 检查缓存是否过期（24小时）
        if (data.timestamp && Date.now() - data.timestamp < 24 * 60 * 60 * 1000) {
          cityList.value = data.list
          return cityList.value!
        }
      } catch (e) {
        console.error('解析城市列表缓存失败', e)
      }
    }

    // 从服务器加载
    const list = await getCityList()
    cityList.value = list

    // 缓存到本地
    uni.setStorageSync(CITY_LIST_CACHE_KEY, JSON.stringify({
      list,
      timestamp: Date.now()
    }))

    return list
  }

  /**
   * 打开城市选择器
   */
  function openCitySelector() {
    showCitySelector.value = true
  }

  /**
   * 关闭城市选择器
   */
  function closeCitySelector() {
    showCitySelector.value = false
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
    // 状态
    currentCity,
    currentCityName,
    currentLocation,
    cityList,
    showCitySelector,
    systemInfo,
    isLocating,
    // 方法
    initCity,
    autoLocate,
    getLocation,
    setCurrentCity,
    setCity,
    loadCityList,
    openCitySelector,
    closeCitySelector,
    getSystemInfo
  }
})
