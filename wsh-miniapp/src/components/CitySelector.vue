<template>
  <view class="city-selector" v-if="visible" @tap.stop>
    <!-- 遮罩层 -->
    <view class="mask" @tap="handleClose"></view>
    
    <!-- 内容区 -->
    <view class="content" :style="{ height: contentHeight }">
      <!-- 头部 -->
      <view class="header">
        <text class="title">选择城市</text>
        <view class="close-btn" @tap="handleClose">
          <text>×</text>
        </view>
      </view>
      
      <!-- 搜索框 -->
      <view class="search-box">
        <view class="search-input-wrap">
          <text class="search-icon">🔍</text>
          <input 
            class="search-input" 
            v-model="searchKeyword" 
            placeholder="搜索城市" 
            placeholder-class="placeholder"
            @input="handleSearch"
          />
          <view class="clear-btn" v-if="searchKeyword" @tap="clearSearch">
            <text>×</text>
          </view>
        </view>
      </view>
      
      <!-- 定位城市 -->
      <view class="locate-section">
        <view class="section-title">
          <text>当前定位</text>
          <view class="relocate-btn" @tap="handleRelocate" v-if="!isLocating">
            <text>重新定位</text>
          </view>
        </view>
        <view class="locate-city" @tap="selectLocatedCity">
          <text v-if="isLocating">定位中...</text>
          <text v-else-if="locatedCity">{{ locatedCity.cityName }}</text>
          <text v-else class="locate-failed">定位失败，点击重试</text>
        </view>
      </view>
      
      <!-- 搜索结果 -->
      <view class="search-result" v-if="searchKeyword && searchResults.length > 0">
        <view class="section-title">
          <text>搜索结果</text>
        </view>
        <view class="city-grid">
          <view 
            class="city-item" 
            v-for="city in searchResults" 
            :key="city.cityId"
            :class="{ active: isSelected(city) }"
            @tap="selectCity(city)"
          >
            {{ city.cityName }}
          </view>
        </view>
      </view>
      
      <!-- 无搜索结果 -->
      <view class="no-result" v-else-if="searchKeyword && searchResults.length === 0">
        <text>未找到匹配的城市</text>
      </view>
      
      <!-- 城市列表（非搜索状态） -->
      <scroll-view 
        v-else
        class="city-list" 
        scroll-y 
        :scroll-into-view="scrollIntoView"
        scroll-with-animation
      >
        <!-- 热门城市 -->
        <view class="hot-section" id="hot">
          <view class="section-title">
            <text>热门城市</text>
          </view>
          <view class="city-grid">
            <view 
              class="city-item" 
              v-for="city in hotCities" 
              :key="city.cityId"
              :class="{ active: isSelected(city) }"
              @tap="selectCity(city)"
            >
              {{ city.cityName }}
            </view>
          </view>
        </view>
        
        <!-- 按字母分组的城市 -->
        <view 
          class="letter-section" 
          v-for="(cities, letter) in sortedAllCities" 
          :key="letter"
          :id="'letter-' + letter"
        >
          <view class="letter-title">{{ letter }}</view>
          <view class="city-grid">
            <view 
              class="city-item" 
              v-for="city in cities" 
              :key="city.cityId"
              :class="{ active: isSelected(city) }"
              @tap="selectCity(city)"
            >
              {{ city.cityName }}
            </view>
          </view>
        </view>
      </scroll-view>
      
      <!-- 字母索引 -->
      <view class="letter-index" v-if="!searchKeyword">
        <view 
          class="letter-item" 
          :class="{ active: currentLetter === '热' }"
          @tap="scrollToLetter('hot')"
        >热</view>
        <view 
          class="letter-item" 
          v-for="letter in letterList" 
          :key="letter"
          :class="{ active: currentLetter === letter }"
          @tap="scrollToLetter('letter-' + letter)"
        >{{ letter }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useAppStore } from '@/store/app'
import { locateCity, type CityItem } from '@/api/city'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'select', city: CityItem): void
}>()

const appStore = useAppStore()

// 状态
const searchKeyword = ref('')
const searchResults = ref<CityItem[]>([])
const locatedCity = ref<CityItem | null>(null)
const isLocating = ref(false)
const scrollIntoView = ref('')
const currentLetter = ref('')

// 计算内容高度（80vh）
const contentHeight = computed(() => {
  const info = appStore.getSystemInfo()
  return `${info.windowHeight * 0.8}px`
})

// 热门城市
const hotCities = computed(() => appStore.cityList?.hotCities || [])

// 所有城市（按字母排序）
const sortedAllCities = computed(() => {
  if (!appStore.cityList?.allCities) return {}
  const sorted: Record<string, CityItem[]> = {}
  const keys = Object.keys(appStore.cityList.allCities).sort()
  for (const key of keys) {
    sorted[key.toUpperCase()] = appStore.cityList.allCities[key]
  }
  return sorted
})

// 字母列表
const letterList = computed(() => Object.keys(sortedAllCities.value))

// 检查城市是否被选中
function isSelected(city: CityItem): boolean {
  return appStore.currentCity?.cityId === city.cityId
}

// 加载城市列表
async function loadCities() {
  try {
    await appStore.loadCityList()
  } catch (err) {
    console.error('加载城市列表失败', err)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 定位当前城市
async function locateCurrentCity() {
  try {
    isLocating.value = true
    const location = await appStore.getLocation()
    locatedCity.value = await locateCity(location.longitude, location.latitude)
  } catch (err) {
    console.error('定位失败', err)
    locatedCity.value = null
  } finally {
    isLocating.value = false
  }
}

// 重新定位
function handleRelocate() {
  locateCurrentCity()
}

// 选择定位城市
function selectLocatedCity() {
  if (locatedCity.value) {
    selectCity(locatedCity.value)
  } else {
    handleRelocate()
  }
}

// 选择城市
function selectCity(city: CityItem) {
  appStore.setCurrentCity(city)
  emit('select', city)
  handleClose()
}

// 关闭选择器
function handleClose() {
  emit('update:visible', false)
}

// 搜索城市
function handleSearch() {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    searchResults.value = []
    return
  }
  
  const results: CityItem[] = []
  
  // 搜索热门城市
  for (const city of hotCities.value) {
    if (matchCity(city, keyword)) {
      results.push(city)
    }
  }
  
  // 搜索所有城市
  for (const cities of Object.values(sortedAllCities.value)) {
    for (const city of cities) {
      if (matchCity(city, keyword) && !results.find(r => r.cityId === city.cityId)) {
        results.push(city)
      }
    }
  }
  
  searchResults.value = results
}

// 匹配城市
function matchCity(city: CityItem, keyword: string): boolean {
  return city.cityName.toLowerCase().includes(keyword) ||
         city.pinyin.toLowerCase().includes(keyword) ||
         city.provinceName.toLowerCase().includes(keyword)
}

// 清空搜索
function clearSearch() {
  searchKeyword.value = ''
  searchResults.value = []
}

// 滚动到指定字母
function scrollToLetter(id: string) {
  scrollIntoView.value = id
  currentLetter.value = id === 'hot' ? '热' : id.replace('letter-', '')
}

// 监听显示状态
watch(() => props.visible, async (val) => {
  if (val) {
    // 加载城市列表
    if (!appStore.cityList) {
      await loadCities()
    }
    // 定位当前城市
    if (!locatedCity.value) {
      locateCurrentCity()
    }
    // 重置搜索
    clearSearch()
  }
})

// 初始化
onMounted(() => {
  if (props.visible) {
    loadCities()
    locateCurrentCity()
  }
})
</script>

<style lang="scss" scoped>
.city-selector {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  
  .mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
  }
  
  .content {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    background: #fff;
    border-radius: 24rpx 24rpx 0 0;
    display: flex;
    flex-direction: column;
  }
  
  .header {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 32rpx;
    position: relative;
    border-bottom: 1rpx solid #f0f0f0;
    
    .title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .close-btn {
      position: absolute;
      right: 32rpx;
      top: 50%;
      transform: translateY(-50%);
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      border-radius: 50%;
      
      text {
        font-size: 36rpx;
        color: #999;
        line-height: 1;
      }
    }
  }
  
  .search-box {
    padding: 20rpx 24rpx;
    
    .search-input-wrap {
      display: flex;
      align-items: center;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 16rpx 20rpx;
      
      .search-icon {
        font-size: 28rpx;
        margin-right: 12rpx;
      }
      
      .search-input {
        flex: 1;
        font-size: 28rpx;
        color: #333;
      }
      
      .placeholder {
        color: #999;
      }
      
      .clear-btn {
        width: 36rpx;
        height: 36rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #ccc;
        border-radius: 50%;
        
        text {
          font-size: 24rpx;
          color: #fff;
          line-height: 1;
        }
      }
    }
  }
  
  .locate-section {
    padding: 0 24rpx 20rpx;
    
    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
      
      text {
        font-size: 26rpx;
        color: #999;
      }
      
      .relocate-btn {
        text {
          font-size: 24rpx;
          color: #667eea;
        }
      }
    }
    
    .locate-city {
      display: inline-block;
      padding: 12rpx 24rpx;
      background: #f5f5f5;
      border-radius: 8rpx;
      font-size: 28rpx;
      color: #333;
      
      .locate-failed {
        color: #999;
      }
    }
  }
  
  .search-result, .no-result {
    padding: 0 24rpx;
  }
  
  .no-result {
    padding: 60rpx 24rpx;
    text-align: center;
    color: #999;
    font-size: 28rpx;
  }
  
  .section-title {
    margin-bottom: 16rpx;
    
    text {
      font-size: 26rpx;
      color: #999;
    }
  }
  
  .city-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  
  .city-item {
    padding: 16rpx 28rpx;
    background: #f5f5f5;
    border-radius: 8rpx;
    font-size: 28rpx;
    color: #333;
    
    &.active {
      background: #667eea;
      color: #fff;
    }
  }
  
  .city-list {
    flex: 1;
    padding: 0 24rpx 24rpx;
    padding-right: 60rpx; // 给字母索引留空间
  }
  
  .hot-section {
    margin-bottom: 32rpx;
  }
  
  .letter-section {
    margin-bottom: 24rpx;
    
    .letter-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #667eea;
      margin-bottom: 16rpx;
      padding-bottom: 8rpx;
      border-bottom: 1rpx solid #f0f0f0;
    }
  }
  
  .letter-index {
    position: absolute;
    right: 8rpx;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4rpx;
    
    .letter-item {
      width: 36rpx;
      height: 36rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20rpx;
      color: #666;
      border-radius: 50%;
      
      &.active {
        background: #667eea;
        color: #fff;
      }
    }
  }
}
</style>
