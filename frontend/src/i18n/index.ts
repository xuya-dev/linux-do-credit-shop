import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN.json'
import enUS from './locales/en-US.json'

/**
 * 国际化配置 / I18n Configuration
 * 支持中英文切换 / Support Chinese/English switching
 */
const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('ldc-shop-locale') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
})

export default i18n
