import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const token = ref<string | null>(localStorage.getItem('ldc-shop-token'))
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  let userFetched = ref(false)
  let fetchPromise: Promise<void> | null = null

  async function fetchUser() {
    if (!token.value) {
      userFetched.value = true
      return
    }
    if (fetchPromise) return fetchPromise
    fetchPromise = (async () => {
      try {
        const res = await authApi.getUserInfo()
        user.value = res.data
      } catch {
        token.value = null
        localStorage.removeItem('ldc-shop-token')
        user.value = null
      } finally {
        userFetched.value = true
      }
    })()
    return fetchPromise
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('ldc-shop-token', newToken)
  }

  async function login(code: string, redirectUri?: string) {
    const res = await authApi.callback(code, redirectUri)
    setToken(res.data.token)
    user.value = res.data.user
    userFetched.value = true
    return res.data
  }

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
    userFetched,
    fetchUser,
    setToken,
    login,
    logout,
  }
})
