<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { announcementApi, productApi } from '#/api/modules';

const router = useRouter();

const products = ref<any[]>([]);
const announcements = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const [prodRes, annRes] = await Promise.all([
      productApi.userList({ page: 1, size: 8 }),
      announcementApi.latest(3),
    ]);
    products.value = prodRes?.records || [];
    announcements.value = annRes || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div>
    <section
      style="
        padding: 80px 24px;
        text-align: center;
        max-width: 1120px;
        margin: 0 auto;
      "
    >
      <p
        style="
          font-size: 11px;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.1em;
          opacity: 0.5;
          margin-bottom: 16px;
        "
      >
        LINUX DO Credit
      </p>
      <h1
        style="
          font-size: 48px;
          font-weight: 700;
          letter-spacing: -0.03em;
          margin-bottom: 16px;
          line-height: 1.1;
        "
      >
        LDC Shop
      </h1>
      <p
        style="
          font-size: 17px;
          opacity: 0.6;
          max-width: 600px;
          margin: 0 auto 32px;
        "
      >
        Exchange goods with LINUX DO credits
      </p>
      <n-button
        type="primary"
        size="large"
        @click="router.push('/products')"
      >
        Browse Products
      </n-button>
    </section>

    <n-divider style="max-width: 1120px; margin: 0 auto" />

    <section
      v-if="announcements.length"
      style="padding: 40px 24px; max-width: 1120px; margin: 0 auto"
    >
      <h2
        style="
          font-size: 28px;
          font-weight: 700;
          letter-spacing: -0.02em;
          margin-bottom: 24px;
        "
      >
        Latest Announcements
      </h2>
      <n-card
        v-for="ann in announcements"
        :key="ann.id"
        hoverable
        style="margin-bottom: 8px; cursor: pointer"
        @click="router.push('/announcement/' + ann.id)"
      >
        <div
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div style="display: flex; align-items: center; gap: 8px">
            <n-tag :type="ann.type === 2 ? 'warning' : ann.type === 3 ? 'info' : 'success'" size="small">
              {{ ann.typeName }}
            </n-tag>
            <span style="font-size: 15px; font-weight: 500">
              {{ ann.title }}
            </span>
          </div>
          <span style="font-size: 13px; opacity: 0.5">
            {{ ann.publishedAt }}
          </span>
        </div>
      </n-card>
    </section>

    <n-divider style="max-width: 1120px; margin: 0 auto" />

    <section style="padding: 80px 24px; max-width: 1120px; margin: 0 auto">
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 32px;
        "
      >
        <h2
          style="
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.02em;
          "
        >
          Featured Products
        </h2>
        <n-button text @click="router.push('/products')">
          More &rarr;
        </n-button>
      </div>

      <n-spin v-if="loading" style="width: 100%; padding: 60px" />

      <n-grid
        v-else-if="products.length"
        :cols="4"
        :x-gap="20"
        :y-gap="20"
        responsive="screen"
        item-responsive
      >
        <n-grid-item v-for="p in products" :key="p.id">
          <n-card
            hoverable
            style="cursor: pointer"
            @click="router.push('/product/' + p.id)"
          >
            <template #cover>
              <div v-if="p.coverImage" style="aspect-ratio: 1; overflow: hidden">
                <img
                  :src="p.coverImage"
                  style="width: 100%; height: 100%; object-fit: cover"
                />
              </div>
              <div
                v-else
                style="
                  aspect-ratio: 1;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background: rgba(128, 128, 128, 0.06);
                  font-size: 40px;
                "
              >
                📦
              </div>
            </template>
            <h3
              style="
                font-size: 15px;
                font-weight: 600;
                margin: 0 0 4px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              "
            >
              {{ p.name }}
            </h3>
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              "
            >
              <span style="font-size: 16px; font-weight: 700; color: #18a058">
                {{ p.price }}
                <span style="font-size: 12px; font-weight: 400">credits</span>
              </span>
              <span style="font-size: 12px; opacity: 0.5">
                Sold {{ p.soldCount }}
              </span>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-empty v-else description="No products yet" style="padding: 60px" />
    </section>
  </div>
</template>
