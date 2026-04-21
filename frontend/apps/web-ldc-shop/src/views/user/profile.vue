<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@vben/stores';
import { useI18n } from '@vben/locales';
import '#/styles/faka-common.css';

const router = useRouter();
const userStore = useUserStore();
const { t } = useI18n();

const userInfo = computed(() => userStore.userInfo);
const isAdmin = computed(() => userInfo.value?.role === 'admin' || userInfo.value?.roles?.includes('admin'));
const trustLabel = computed(() => {
  const level = userInfo.value?.trustLevel ?? 0;
  const labels = [t('page.user.newUser'), t('page.user.basicTrust'), t('page.user.generalTrust'), t('page.user.highTrust'), t('page.user.coreUser')];
  return labels[level] || t('page.user.trustLevel', { level });
});

const menuItems = computed(() => [
  { label: t('page.user.myOrdersMenu'), path: '/orders', iconImg: '/订单.png', desc: t('page.user.myOrdersDesc') },
  { label: t('page.user.afterSaleMenu'), path: '/disputes', iconImg: '/售后.png', desc: t('page.user.afterSaleDesc') },
  { label: t('page.user.siteAnnouncementsMenu'), path: '/announcements', iconImg: '/公告.png', desc: t('page.user.siteAnnouncementsDesc') },
]);
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>{{ t('page.user.home') }}</span> &gt; <span>{{ t('page.user.profile') }}</span>
    </div>

    <!-- 用户基本信息卡片 -->
    <div class="faka-card profile-header-card">
      <div class="profile-avatar-wrap">
        <div class="profile-avatar">
          <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" loading="lazy" />
          <span v-else>{{ (userInfo?.username?.[0] || userInfo?.nickname?.[0] || 'U').toUpperCase() }}</span>
        </div>
      </div>
      <div class="profile-info">
        <div class="profile-title">
          <span class="profile-name">{{ userInfo?.username || t('page.user.userDefault') }}</span>
          <span :class="['faka-tag', isAdmin ? 'admin-tag' : 'user-tag']">
            {{ isAdmin ? t('page.user.adminRole') : t('page.user.normalMember') }}
          </span>
          <span class="faka-tag trust-tag">{{ trustLabel }}</span>
        </div>
        <div class="profile-meta">
          <span>{{ t('page.user.registerAccount') }} {{ userInfo?.email || '-' }}</span>
          <span class="divider">|</span>
          <span>{{ t('page.user.joinTime') }} {{ userInfo?.createdAt ? new Date(userInfo.createdAt).toLocaleDateString('zh-CN') : '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="faka-card mt-24">
      <div class="card-header">{{ t('page.user.serviceHall') }}</div>
      <div class="card-body">
        <div class="service-grid">
          <div 
            v-for="item in menuItems" 
            :key="item.path"
            class="service-box"
            @click="router.push(item.path)"
          >
            <div class="service-icon">
              <img v-if="item.iconImg" :src="item.iconImg" alt="icon" class="service-icon-img" loading="lazy" />
              <span v-else>{{ item.icon }}</span>
            </div>
            <div class="service-text">
              <span class="service-label">{{ item.label }}</span>
              <span class="service-desc">{{ item.desc }}</span>
            </div>
            <div class="service-arrow">&gt;</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 后台入口 -->
    <div v-if="isAdmin" class="faka-card mt-24 admin-panel">
      <div class="admin-flex">
        <div class="admin-meta">
          <div class="admin-meta-title">{{ t('page.user.systemAdminPanel') }}</div>
          <div class="admin-meta-desc">{{ t('page.user.adminPanelDesc') }}</div>
        </div>
        <button class="faka-btn primary" @click="router.push('/admin/dashboard')">
          {{ t('page.user.enterAdminPanel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 1000px; }
.breadcrumb { margin-bottom: 20px; }

.card-body { padding: 20px; }

/* 个人信息栏 */
.profile-header-card {
  display: flex;
  align-items: center;
  padding: 30px;
  gap: 24px;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #1890ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  overflow: hidden;
}
.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-title {
  display: flex;
  align-items: center;
  gap: 12px;
}
.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--faka-text-main, #333);
}

.faka-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 2px;
}
.admin-tag { background: var(--faka-tag-active-bg); color: #faad14; border: 1px solid #faad14; }
.user-tag { background: var(--faka-tag-bg); color: #52c41a; border: 1px solid #b7eb8f; }
.trust-tag { background: var(--faka-primary-bg); color: var(--faka-primary-color); border: 1px solid var(--faka-primary-color); }

.profile-meta {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
  display: flex;
  align-items: center;
  gap: 12px;
}
.divider { color: var(--faka-border, #e8e8e8); }

/* 服务大厅 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.service-box {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid var(--faka-border, #e8e8e8);
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--faka-tag-bg, #fafafa);
}
.service-box:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}
.service-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 16px;
}
.service-icon-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}
.service-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.service-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--faka-text-main, #333);
}
.service-desc {
  font-size: 12px;
  color: var(--faka-text-sub, #8c8c8c);
}
.service-arrow {
  color: var(--faka-border, #d9d9d9);
  font-family: monospace;
}
.service-box:hover .service-arrow {
  color: #1890ff;
}

/* 管理后台 */
.admin-panel {
  background: var(--faka-tag-bg);
  border: 1px solid #faad14;
  color: #faad14;
}
.admin-flex {
  padding: 20px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.admin-meta-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}
.admin-meta-desc {
  font-size: 13px;
  opacity: 0.8;
}

/* Override faka-btn primary for profile admin context */
.faka-btn.primary {
  background: #faad14;
  color: #fff;
}
.faka-btn {
  padding: 8px 24px;
}

@media (max-width: 768px) {
  .service-grid { grid-template-columns: 1fr; }
  .admin-flex { flex-direction: column; text-align: center; gap: 16px; }
  .profile-header-card { flex-direction: column; text-align: center; }
  .profile-title { flex-direction: column; }
  .profile-meta { flex-direction: column; gap: 4px; }
  .divider { display: none; }
}
</style>
