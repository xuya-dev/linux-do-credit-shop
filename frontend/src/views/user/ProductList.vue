<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { productApi, categoryApi } from '@/api'

const { t } = useI18n()
const router = useRouter()

const products = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const selectedCategory = ref<number | null>(null)
const selectedType = ref<number | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await productApi.userList({
      page: page.value,
      size: 12,
      categoryId: selectedCategory.value || undefined,
      keyword: keyword.value || undefined,
      productType: selectedType.value || undefined,
    })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const catRes = await categoryApi.userList()
  categories.value = catRes.data || []
  await loadData()
})

watch([selectedCategory, selectedType], () => {
  page.value = 1
  loadData()
})

function searchProducts() {
  page.value = 1
  loadData()
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <!-- 标题 / Title -->
    <div style="margin-bottom: 32px;">
      <h1 style="font-size: 32px; font-weight: 700; letter-spacing: -0.02em; color: var(--color-text-dark);">{{ t('product.title') }}</h1>
    </div>

    <!-- 筛选栏 / Filter Bar -->
    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 24px; flex-wrap: wrap;">
      <!-- 搜索 / Search -->
      <div style="display: flex; align-items: center; gap: 8px; border: 1px solid var(--color-border); border-radius: 8px; padding: 6px 12px; flex: 1; max-width: 320px;">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--color-text-light)" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="keyword" @keyup.enter="searchProducts" :placeholder="t('product.search')" style="border: none; outline: none; font-size: 14px; width: 100%; background: transparent; color: var(--color-text-dark);" />
      </div>

      <!-- 分类 / Category -->
      <select v-model="selectedCategory" style="border: 1px solid var(--color-border); border-radius: 8px; padding: 8px 12px; font-size: 14px; background: var(--color-bg); color: var(--color-text-dark); cursor: pointer;">
        <option :value="null">{{ t('product.allCategories') }}</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>

      <!-- 类型 / Type -->
      <select v-model="selectedType" style="border: 1px solid var(--color-border); border-radius: 8px; padding: 8px 12px; font-size: 14px; background: var(--color-bg); color: var(--color-text-dark); cursor: pointer;">
        <option :value="null">{{ t('product.allTypes') }}</option>
        <option :value="1">{{ t('product.virtual') }}</option>
        <option :value="2">{{ t('product.physical') }}</option>
      </select>
    </div>

    <!-- 商品网格 / Product Grid -->
    <div v-if="loading" style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.loading') }}</div>

    <div v-else-if="products.length" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;">
      <div v-for="p in products" :key="p.id" @click="router.push('/product/' + p.id)" class="feature-card" style="cursor: pointer;">
        <div v-if="p.coverImage" style="width: 100%; aspect-ratio: 1; border-radius: 8px; overflow: hidden; margin-bottom: 12px;">
          <img :src="p.coverImage" style="width: 100%; height: 100%; object-fit: cover;" />
        </div>
        <div v-else style="width: 100%; aspect-ratio: 1; border-radius: 8px; background: var(--color-bg-secondary); display: flex; align-items: center; justify-content: center; margin-bottom: 12px;">
          <span style="color: var(--color-text-light); font-size: 32px;">📦</span>
        </div>
        <div style="display: flex; gap: 6px; margin-bottom: 6px;">
          <span class="badge" :class="p.productType === 1 ? 'badge-info' : 'badge-success'">{{ p.productType === 1 ? t('product.virtual') : t('product.physical') }}</span>
        </div>
        <h3 style="font-size: 15px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 4px;">{{ p.name }}</h3>
        <p style="font-size: 13px; color: var(--color-text-light); margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">{{ p.description }}</p>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: 700; color: var(--color-primary);">{{ p.price }}</span>
          <span style="font-size: 12px; color: var(--color-text-light);">{{ t('product.stock') }}: {{ p.stock }} | {{ t('product.sold') }}: {{ p.soldCount }}</span>
        </div>
      </div>
    </div>

    <div v-else style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.noData') }}</div>

    <!-- 分页 / Pagination -->
    <div v-if="total > 12" style="display: flex; justify-content: center; gap: 8px; margin-top: 32px;">
      <button v-for="p in Math.ceil(total / 12)" :key="p" @click="page = p; loadData()" :style="{ padding: '6px 12px', borderRadius: '6px', border: page === p ? '1px solid var(--color-primary)' : '1px solid var(--color-border)', background: page === p ? 'var(--color-primary)' : 'transparent', color: page === p ? 'white' : 'var(--color-text-mid)', cursor: 'pointer', fontSize: '14px' }">{{ p }}</button>
    </div>
  </div>
</template>
