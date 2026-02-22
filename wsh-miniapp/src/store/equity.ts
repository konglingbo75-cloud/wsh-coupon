/**
 * 权益状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  getEquitySummary, 
  getExpiringEquities,
  type EquitySummary, 
  type ExpiringEquity 
} from '@/api/equity'

export const useEquityStore = defineStore('equity', () => {
  // 状态
  const summary = ref<EquitySummary | null>(null)
  const expiringList = ref<ExpiringEquity[]>([])
  const loading = ref(false)
  const lastFetch = ref<number>(0)

  // 计算属性
  const totalValue = computed(() => {
    if (!summary.value) return 0
    return summary.value.totalPointsValue + 
           summary.value.totalBalance + 
           summary.value.totalVoucherValue
  })

  const hasExpiring = computed(() => {
    return expiringList.value.length > 0
  })

  const expiringCount = computed(() => {
    return expiringList.value.length
  })

  /**
   * 获取权益总览
   */
  async function fetchSummary(force: boolean = false) {
    // 5分钟缓存
    if (!force && Date.now() - lastFetch.value < 5 * 60 * 1000 && summary.value) {
      return summary.value
    }

    loading.value = true
    try {
      summary.value = await getEquitySummary()
      lastFetch.value = Date.now()
      return summary.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取即将过期权益
   */
  async function fetchExpiring() {
    loading.value = true
    try {
      expiringList.value = await getExpiringEquities()
      return expiringList.value
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空数据
   */
  function clear() {
    summary.value = null
    expiringList.value = []
    lastFetch.value = 0
  }

  return {
    // 状态
    summary,
    expiringList,
    loading,
    // 计算属性
    totalValue,
    hasExpiring,
    expiringCount,
    // 方法
    fetchSummary,
    fetchExpiring,
    clear
  }
})
