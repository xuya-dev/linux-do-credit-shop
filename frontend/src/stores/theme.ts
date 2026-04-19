import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 主题模式 / Theme Mode
 * - light: 浅色 / Light
 * - dark: 深色 / Dark
 */
export type ThemeMode = 'light' | 'dark'

/**
 * 主题状态管理 / Theme Store
 * 管理深浅主题切换
 */
export const useThemeStore = defineStore('theme', () => {
  /** 当前主题 / Current theme */
  const mode = ref<ThemeMode>(
    (localStorage.getItem('ldc-shop-theme') as ThemeMode) || 'light'
  )

  /**
   * 切换主题 / Toggle theme
   */
  function toggleTheme() {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('ldc-shop-theme', mode.value)
    applyTheme()
  }

  /**
   * 设置主题 / Set theme
   */
  function setTheme(theme: ThemeMode) {
    mode.value = theme
    localStorage.setItem('ldc-shop-theme', theme)
    applyTheme()
  }

  /**
   * 应用主题到 DOM / Apply theme to DOM
   */
  function applyTheme() {
    const html = document.documentElement
    if (mode.value === 'dark') {
      html.setAttribute('data-theme', 'dark')
    } else {
      html.removeAttribute('data-theme')
    }
  }

  // 初始化时应用主题 / Apply theme on init
  applyTheme()

  return { mode, toggleTheme, setTheme }
})
