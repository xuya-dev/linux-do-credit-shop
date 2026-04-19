<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { productApi, orderApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const product = ref<any>(null)
const loading = ref(true)
const buying = ref(false)
const quantity = ref(1)
const showBuyModal = ref(false)
const contactInfo = ref('')
const remark = ref('')

async function loadProduct() {
  try {
    const res = await productApi.detail(Number(route.params.id))
    product.value = res.data
  } catch (e) {
    message.error('加载失败 / Load failed')
  } finally {
    loading.value = false
  }
}

onMounted(loadProduct)

async function handleBuy() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  buying.value = true
  try {
    // 创建订单 / Create order
    const orderRes = await orderApi.create({
      productId: product.value.id,
      quantity: quantity.value,
      contactInfo: contactInfo.value,
      remark: remark.value,
    })
    // 发起支付 / Initiate payment
    const payRes = await orderApi.pay(orderRes.data.id)
    if (payRes.data?.payUrl) {
      window.location.href = payRes.data.payUrl
    } else {
      message.success(t('common.success'))
      router.push('/orders')
    }
  } catch (e: any) {
    message.error(e.message || t('common.failed'))
  } finally {
    buying.value = false
    showBuyModal.value = false
  }
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>

    <div v-else-if="product" style="display: grid; grid-template-columns: 1fr 1fr; gap: 48px;">
      <!-- 图片区 / Images -->
      <div>
        <div style="border-radius: 10px; overflow: hidden; background: var(--color-bg-secondary); aspect-ratio: 1;">
          <img v-if="product.coverImage" :src="product.coverImage" style="width: 100%; height: 100%; object-fit: cover;" />
          <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;">
            <span style="font-size: 64px; color: var(--color-text-light);">📦</span>
          </div>
        </div>
      </div>

      <!-- 信息区 / Info -->
      <div>
        <div style="display: flex; gap: 8px; margin-bottom: 12px;">
          <span class="badge" :class="product.productType === 1 ? 'badge-info' : 'badge-success'">{{ product.productType === 1 ? t('product.virtual') : t('product.physical') }}</span>
          <span v-if="product.categoryName" class="badge badge-info">{{ product.categoryName }}</span>
        </div>

        <h1 style="font-size: 28px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 12px;">{{ product.name }}</h1>
        <p style="font-size: 16px; color: var(--color-text-mid); margin-bottom: 24px; line-height: 1.65;">{{ product.description }}</p>

        <!-- 价格 / Price -->
        <div style="background: var(--color-bg-secondary); border-radius: 10px; padding: 20px; margin-bottom: 24px;">
          <span style="font-size: 36px; font-weight: 700; color: var(--color-primary);">{{ product.price }}</span>
          <span style="font-size: 14px; color: var(--color-text-light); margin-left: 4px;">积分 / Credits</span>
        </div>

        <!-- 库存信息 / Stock Info -->
        <div style="display: flex; gap: 24px; margin-bottom: 24px; font-size: 14px; color: var(--color-text-mid);">
          <span>{{ t('product.stock') }}: <strong style="color: var(--color-text-dark);">{{ product.stock }}</strong></span>
          <span>{{ t('product.sold') }}: <strong style="color: var(--color-text-dark);">{{ product.soldCount }}</strong></span>
        </div>

        <!-- 购买操作 / Buy Action -->
        <div v-if="product.status === 1" style="display: flex; align-items: center; gap: 16px;">
          <div style="display: flex; align-items: center; gap: 8px; border: 1px solid var(--color-border); border-radius: 8px; padding: 4px;">
            <button @click="quantity = Math.max(1, quantity - 1)" style="width: 32px; height: 32px; border: none; background: none; cursor: pointer; font-size: 16px; color: var(--color-text-mid);">−</button>
            <span style="width: 32px; text-align: center; font-weight: 600; color: var(--color-text-dark);">{{ quantity }}</span>
            <button @click="quantity++" style="width: 32px; height: 32px; border: none; background: none; cursor: pointer; font-size: 16px; color: var(--color-text-mid);">+</button>
          </div>
          <button @click="showBuyModal = true" style="background: var(--color-primary); color: white; border: none; padding: 12px 32px; border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; flex: 1;">
            {{ t('product.buyNow') }}
          </button>
        </div>
        <div v-else>
          <span class="badge badge-error" style="font-size: 14px; padding: 6px 16px;">{{ t('product.offShelf') }}</span>
        </div>
      </div>
    </div>

    <!-- 购买弹窗 / Buy Modal -->
    <div v-if="showBuyModal" style="position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100;" @click.self="showBuyModal = false">
      <div style="background: var(--color-bg); border-radius: 10px; padding: 32px; width: 480px; max-width: 90vw;">
        <h3 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 20px;">{{ t('product.buyNow') }}</h3>
        <div style="margin-bottom: 12px;">
          <label style="font-size: 13px; font-weight: 500; color: var(--color-text-mid); display: block; margin-bottom: 4px;">{{ t('order.contactInfo') }}</label>
          <input v-model="contactInfo" style="width: 100%; padding: 8px 12px; border: 1px solid var(--color-border); border-radius: 8px; font-size: 14px; background: var(--color-bg); color: var(--color-text-dark);" />
        </div>
        <div style="margin-bottom: 20px;">
          <label style="font-size: 13px; font-weight: 500; color: var(--color-text-mid); display: block; margin-bottom: 4px;">{{ t('order.remark') }}</label>
          <textarea v-model="remark" rows="3" style="width: 100%; padding: 8px 12px; border: 1px solid var(--color-border); border-radius: 8px; font-size: 14px; resize: vertical; background: var(--color-bg); color: var(--color-text-dark);"></textarea>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button @click="showBuyModal = false" style="padding: 8px 20px; border: 1px solid var(--color-border); border-radius: 8px; background: none; cursor: pointer; font-size: 14px; color: var(--color-text-mid);">{{ t('common.cancel') }}</button>
          <button @click="handleBuy" :disabled="buying" style="padding: 8px 20px; background: var(--color-primary); color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 500;">
            {{ buying ? t('app.loading') : t('common.confirm') + ' (' + (product?.price * quantity) + ' 积分)' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
