/**
 * 城市服务 API
 */
import { get, post } from './request'

/**
 * 城市信息
 */
export interface CityItem {
  cityId: number
  cityCode: string
  cityName: string
  provinceName: string
  pinyin: string
  level: number
  longitude: number
  latitude: number
  merchantCount: number
  activityCount: number
  status: number
  sortOrder: number
  openDate: string
}

/**
 * 城市列表响应
 */
export interface CityListResponse {
  hotCities: CityItem[]
  allCities: Record<string, CityItem[]>  // 按拼音首字母分组
  total: number
}

/**
 * 获取城市列表
 */
export function getCityList(): Promise<CityListResponse> {
  return get<CityListResponse>('/v1/public/cities')
}

/**
 * 根据GPS定位获取城市
 */
export function locateCity(longitude: number, latitude: number): Promise<CityItem> {
  return post<CityItem>('/v1/public/locate-city', { longitude, latitude })
}

/**
 * 根据城市名获取城市信息
 */
export function getCityByName(cityName: string): Promise<CityItem> {
  return get<CityItem>(`/v1/public/cities/${encodeURIComponent(cityName)}`)
}
