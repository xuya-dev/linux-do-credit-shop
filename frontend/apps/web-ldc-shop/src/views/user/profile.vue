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

const menuItems = [
  { label: '我的订单', path: '/orders', icon: '📦', desc: '查看历史购买与卡密' },
  { label: '售后争议', path: '/disputes', icon: '⚖️', desc: '处理问题订单反馈' },
  { label: '站内公告', path: '/announcements', icon: '📢', desc: '平台最新通知和动态' },
];
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>首页</span> &gt; <span>个人中心</span>
    </div>

    <!-- 用户基本信息卡片 -->
    <div class="faka-card profile-header-card">
      <div class="profile-avatar-wrap">
        <div class="profile-avatar">
          <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" />
          <span v-else>{{ (userInfo?.username?.[0] || 'U').toUpperCase() }}</span>
        </div>
      </div>
      <div class="profile-info">
        <div class="profile-title">
          <span class="profile-name">{{ userInfo?.username || '用户' }}</span>
          <span :class="['faka-tag', isAdmin ? 'admin-tag' : 'user-tag']">
            {{ isAdmin ? '管理员' : '普通会员' }}
          </span>
          <span class="faka-tag trust-tag">{{ trustLabel }}</span>
        </div>
        <div class="profile-meta">
          <span>注册账户: {{ userInfo?.email || '-' }}</span>
          <span class="divider">|</span>
          <span>加入时间: {{ userInfo?.createdAt ? new Date(userInfo.createdAt).toLocaleDateString('zh-CN') : '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="faka-card mt-24">
      <div class="card-header">服务大厅</div>
      <div class="card-body">
        <div class="service-grid">
          <div 
            v-for="item in menuItems" 
            :key="item.path"
            class="service-box"
            @click="router.push(item.path)"
          >
            <div class="service-icon">{{ item.icon }}</div>
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
          <div class="admin-meta-title">系统管理后台</div>
          <div class="admin-meta-desc">您具有管理员权限，可以进入后台管理商品和订单。</div>
        </div>
        <button class="faka-btn primary" @click="router.push('/admin/dashboard')">
          进入管理后台
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
  margin-bottom: 20px;
  cursor: pointer;
}
.breadcrumb span:hover { color: #1890ff; }

.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
}

.mt-24 { margin-top: 24px; }

.card-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
}

.card-body {
  padding: 20px;
}

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
.admin-tag { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.user-tag { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.trust-tag { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }

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
  font-size: 28px;
  margin-right: 16px;
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
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #d46b08;
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

.faka-btn {
  padding: 8px 24px;
  font-size: 14px;
  border-radius: 2px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: opacity 0.2s;
}
.faka-btn.primary {
  background: #faad14;
  color: #fff;
}
.faka-btn.primary:hover { opacity: 0.85; }

@media (max-width: 768px) {
  .service-grid { grid-template-columns: 1fr; }
  .admin-flex { flex-direction: column; text-align: center; gap: 16px; }
  .profile-header-card { flex-direction: column; text-align: center; }
  .profile-title { flex-direction: column; }
  .profile-meta { flex-direction: column; gap: 4px; }
  .divider { display: none; }
}
</style>
