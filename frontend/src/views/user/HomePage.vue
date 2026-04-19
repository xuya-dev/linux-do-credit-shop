<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi, productApi, announcementApi } from '@/api'

const { t } = useI18n()
const router = useRouter()

const products = ref<any[]>([])
const announcements = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [prodRes, annRes] = await Promise.all([
      productApi.userList({ page: 1, size: 8 }),
      announcementApi.latest(5),
    ])
    products.value = prodRes.data?.records || []
    announcements.value = annRes.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <!-- Hero / 英雄区 -->
    <section style="padding: 80px 24px; text-align: center; max-width: 1120px; margin: 0 auto;">
      <p style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.1em; color: var(--color-primary); margin-bottom: 16px;">
        LINUX DO Credit
      </p>
      <h1 style="font-size: 48px; font-weight: 700; letter-spacing: -0.03em; color: var(--color-text-dark); margin-bottom: 16px; line-height: 1.1;">
        {{ t('app.subtitle') }}
      </h1>
      <p style="font-size: 17px; color: var(--color-text-mid); max-width: 600px; margin: 0 auto 32px;">
        使用 LINUX DO 积分兑换商品 / Exchange goods with LINUX DO credits
      </p>
      <button @click="router.push('/products')" style="background: var(--color-primary); color: white; border: none; padding: 12px 32px; border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer;">
        {{ t('nav.products') }} →
      </button>
    </section>

    <hr class="section-divider" style="max-width: 1120px; margin: 0 auto;" />

    <!-- 最新公告 / Latest Announcements -->
    <section v-if="announcements.length" style="padding: 40px 24px; max-width: 1120px; margin: 0 auto;">
      <h2 style="font-size: 28px; font-weight: 700; letter-spacing: -0.02em; color: var(--color-text-dark); margin-bottom: 24px;">
        {{ t('announcement.latest') }}
      </h2>
      <div style="display: flex; flex-direction: column; gap: 8px;">
        <div
          v-for="ann in announcements"
          :key="ann.id"
          @click="router.push('/announcement/' + ann.id)"
          style="display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--color-border); cursor: pointer;"
        >
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="badge" :class="ann.type === 2 ? 'badge-warning' : ann.type === 3 ? 'badge-info' : 'badge-success'" style="font-size: 11px;">{{ ann.typeName }}</span>
            <span style="font-size: 15px; color: var(--color-text-dark); font-weight: 500;">{{ ann.title }}</span>
          </div>
          <span style="font-size: 13px; color: var(--color-text-light);">{{ ann.publishedAt }}</span>
        </div>
      </div>
    </section>

    <hr class="section-divider" style="max-width: 1120px; margin: 0 auto;" />

    <!-- 热门商品 / Featured Products -->
    <section style="padding: 80px 24px; max-width: 1120px; margin: 0 auto;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px;">
        <h2 style="font-size: 28px; font-weight: 700; letter-spacing: -0.02em; color: var(--color-text-dark);">
          {{ t('product.title') }}
        </h2>
        <a @click.prevent="router.push('/products')" style="font-size: 14px; font-weight: 500;">{{ t('common.more') }} →</a>
      </div>

      <div v-if="loading" style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.loading') }}</div>

      <div v-else-if="products.length" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;">
        <div
          v-for="p in products"
          :key="p.id"
          @click="router.push('/product/' + p.id)"
          class="feature-card"
          style="cursor: pointer;"
        >
          <div v-if="p.coverImage" style="width: 100%; aspect-ratio: 1; border-radius: 8px; overflow: hidden; margin-bottom: 12px; background: var(--color-bg-secondary);">
            <img :src="p.coverImage" style="width: 100%; height: 100%; object-fit: cover;" />
          </div>
          <div v-else style="width: 100%; aspect-ratio: 1; border-radius: 8px; background: var(--color-bg-secondary); display: flex; align-items: center; justify-content: center; margin-bottom: 12px;">
            <span style="color: var(--color-text-light); font-size: 32px;">📦</span>
          </div>
          <h3 style="font-size: 15px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">{{ p.name }}</h3>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 16px; font-weight: 700; color: var(--color-primary);">{{ p.price }} <span style="font-size: 12px; font-weight: 400;">积分</span></span>
            <span style="font-size: 12px; color: var(--color-text-light);">{{ t('product.sold') }} {{ p.soldCount }}</span>
          </div>
        </div>
      </div>

      <div v-else style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.noData') }}</div>
    </section>
  </div>
</template>
