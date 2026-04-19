<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@vben/stores';

import { getUserInfoApi } from '#/api';

const router = useRouter();
const userStore = useUserStore();

const userInfo = userStore.userInfo || {};

const avatar = userInfo.avatar || '';
const username = userInfo.username || userInfo.realName || '-';
const role = userInfo.roles?.[0] || 'user';
const nickname = userInfo.nickName || userInfo.realName || '-';
const email = userInfo.email || '-';
const trustLevel = userInfo.trustLevel ?? '-';
const createdAt = userInfo.createdAt || userInfo.desc || '-';
const isAdmin = role === 'admin' || userInfo.roles?.includes('admin');
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <h1
      style="
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 32px;
      "
    >
      My Profile
    </h1>

    <n-card v-if="userInfo">
      <div
        style="
          display: flex;
          gap: 24px;
          align-items: center;
          margin-bottom: 24px;
        "
      >
        <n-avatar
          v-if="avatar"
          :src="avatar"
          :size="80"
          round
        />
        <n-avatar v-else :size="80" round>
          {{ (username || 'U')[0].toUpperCase() }}
        </n-avatar>
        <div>
          <h2
            style="
              font-size: 24px;
              font-weight: 700;
              margin: 0 0 8px;
            "
          >
            {{ username }}
          </h2>
          <n-tag :type="isAdmin ? 'info' : 'success'" size="small">
            {{ isAdmin ? 'Admin' : 'User' }}
          </n-tag>
        </div>
      </div>

      <n-descriptions label-placement="top" :column="2" bordered>
        <n-descriptions-item label="Nickname">
          {{ nickname }}
        </n-descriptions-item>
        <n-descriptions-item label="Email">
          {{ email }}
        </n-descriptions-item>
        <n-descriptions-item label="Trust Level">
          {{ trustLevel }}
        </n-descriptions-item>
        <n-descriptions-item label="Joined">
          {{ createdAt }}
        </n-descriptions-item>
      </n-descriptions>

      <div
        v-if="isAdmin"
        style="
          margin-top: 24px;
          padding-top: 24px;
          border-top: 1px solid rgba(128, 128, 128, 0.1);
        "
      >
        <n-button type="primary" @click="router.push('/admin/dashboard')">
          Admin Panel &rarr;
        </n-button>
      </div>
    </n-card>

    <n-empty v-else description="Not logged in" style="padding: 60px" />
  </div>
</template>
