<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@vben/stores';

const router = useRouter();
const userStore = useUserStore();

const userInfo = computed(() => userStore.userInfo);
const isAdmin = computed(() => userInfo.value?.roles?.includes('admin'));
const trustLabel = computed(() => {
  const level = userInfo.value?.trustLevel ?? 0;
  const labels = ['新用户', '基本信任', '一般信任', '高度信任', '核心用户'];
  return labels[level] || `信任等级 ${level}`;
});

const stats = computed(() => [
  { label: '信任等级', value: trustLabel.value, icon: '🛡️' },
  { label: '用户角色', value: isAdmin.value ? '管理员' : '普通用户', icon: '👤' },
  {
    label: '注册时间',
    value: userInfo.value?.createdAt
      ? new Date(userInfo.value.createdAt).toLocaleDateString('zh-CN')
      : '-',
    icon: '📅',
  },
]);

const menuItems = [
  { label: '我的订单', path: '/orders', icon: '📦', desc: '查看所有订单' },
  { label: '我的争议', path: '/disputes', icon: '⚖️', desc: '管理争议记录' },
  { label: '公告中心', path: '/announcements', icon: '📢', desc: '查看最新公告' },
];
</script>

<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="profile-avatar-section">
        <n-avatar
          :size="80"
          round
          :src="userInfo?.avatar"
          class="profile-avatar"
        >
          {{ (userInfo?.username?.[0] || 'U').toUpperCase() }}
        </n-avatar>
        <div class="profile-info">
          <h1 class="profile-name">{{ userInfo?.username || '用户' }}</h1>
          <p class="profile-email">{{ userInfo?.email || '-' }}</p>
          <div class="profile-tags">
            <n-tag :type="isAdmin ? 'info' : 'success'" size="small">
              {{ isAdmin ? '管理员' : '普通用户' }}
            </n-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <n-card
        v-for="stat in stats"
        :key="stat.label"
        class="stat-card"
      >
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </n-card>
    </div>

    <!-- 快捷入口 -->
    <div class="menu-section">
      <h2 class="section-title">快捷入口</h2>
      <div class="menu-grid">
        <n-card
          v-for="item in menuItems"
          :key="item.path"
          hoverable
          class="menu-card"
          @click="router.push(item.path)"
        >
          <div class="menu-icon">{{ item.icon }}</div>
          <div class="menu-content">
            <div class="menu-label">{{ item.label }}</div>
            <div class="menu-desc">{{ item.desc }}</div>
          </div>
          <div class="menu-arrow">→</div>
        </n-card>
      </div>
    </div>

    <!-- 管理员入口 -->
    <div v-if="isAdmin" class="admin-section">
      <n-card class="admin-card">
        <div class="admin-content">
          <div class="admin-icon">⚙️</div>
          <div class="admin-info">
            <div class="admin-title">管理后台</div>
            <div class="admin-desc">进入管理后台进行系统管理</div>
          </div>
        </div>
        <n-button
          type="primary"
          @click="router.push('/admin/dashboard')"
        >
          进入后台 →
        </n-button>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

.profile-header {
  margin-bottom: 32px;
}

.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  font-size: 32px;
  font-weight: 700;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
}

.profile-email {
  font-size: 14px;
  opacity: 0.5;
  margin: 0 0 8px;
}

.profile-tags {
  display: flex;
  gap: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  text-align: center;
  padding: 8px;
}

.stat-icon {
  font-size: 32px;
  line-height: 1;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.5;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
}

.menu-section {
  margin-bottom: 32px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.menu-card {
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.menu-card:hover {
  transform: translateY(-2px);
}

.menu-icon {
  font-size: 28px;
  line-height: 1;
}

.menu-content {
  flex: 1;
}

.menu-label {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.menu-desc {
  font-size: 12px;
  opacity: 0.5;
}

.menu-arrow {
  font-size: 18px;
  opacity: 0.3;
  transition: opacity 0.2s, transform 0.2s;
}

.menu-card:hover .menu-arrow {
  opacity: 0.6;
  transform: translateX(4px);
}

.admin-card :deep(.n-card__content) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-icon {
  font-size: 32px;
  line-height: 1;
}

.admin-title {
  font-size: 16px;
  font-weight: 600;
}

.admin-desc {
  font-size: 13px;
  opacity: 0.5;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }

  .profile-avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .admin-card :deep(.n-card__content) {
    flex-direction: column;
    text-align: center;
  }
}
</style>
