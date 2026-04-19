import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useMessage } from 'naive-ui'

/**
 * API 响应类型 / API Response Type
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页响应类型 / Paginated Response Type
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * HTTP 客户端实例 / HTTP Client Instance
 */
const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器 / Request Interceptor
 * 自动附加 Authorization 令牌
 */
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('ldc-shop-token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 响应拦截器 / Response Interceptor
 * 统一处理错误码
 */
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    if (data.code !== 200) {
      // 401 未登录 / Unauthorized
      if (data.code === 401) {
        localStorage.removeItem('ldc-shop-token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(data.message || 'Request failed'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('ldc-shop-token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 通用请求方法 / Generic request methods
 */
export const request = {
  get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    return http.get(url, { params }).then((res) => res.data)
  },

  post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return http.post(url, data).then((res) => res.data)
  },

  put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return http.put(url, data).then((res) => res.data)
  },

  delete<T = any>(url: string): Promise<ApiResponse<T>> {
    return http.delete(url).then((res) => res.data)
  },
}

export default http
