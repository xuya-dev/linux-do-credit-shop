import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

/**
 * 认证状态管理 / Auth Store
 * 管理用户登录状态、用户信息
 */
export const useAuthStore = defineStore('auth', () => {
  /** 用户信息 / User Info */
  const user = ref<any>(null)

  /** 令牌 / Token */
  const token = ref<string | null>(localStorage.getItem('ldc-shop-token'))

  /** 是否已登录 / Is logged in */
  const isLoggedIn = computed(() => !!token.value)

  /** 是否为管理员 / Is admin */
  const isAdmin = computed(() => user.value?.role === 'admin')

  /**
   * 初始化 - 获取用户信息 / Initialize - fetch user info
   */
  async function fetchUser() {
    if (!token.value) return
    try {
      const res = await authApi.getUserInfo()
      user.value = res.data
    } catch {
      token.value = null
      localStorage.removeItem('ldc-shop-token')
      user.value = null
    }
  }

  /**
   * 设置令牌 / Set token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('ldc-shop-token', newToken)
  }

  /**
   * 登录 / Login
   */
  async function login(code: string, redirectUri?: string) {
    const res = await authApi.callback(code, redirectUri)
    setToken(res.data.token)
    user.value = res.data.user
    return res.data
  }

  /**
   * 退出登录 / Logout
   */
  async function logout() {
    try {
      await authApi.logout()
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('ldc-shop-token')
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    fetchUser,
    setToken,
    login,
    logout,
  }
})
