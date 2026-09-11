<template>
  <el-container class="seller-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '72px' : '236px'" class="aside">
      <div class="logo animate-fade-in-up">
        <div class="logo-badge">
          <el-icon :size="22"><Goods /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">卖家中心</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="side-menu animate-fade-in-up stagger-1"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <template #title>商品管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/customers">
          <el-icon><User /></el-icon>
          <template #title>我的客户</template>
        </el-menu-item>
        <el-menu-item index="/admin/profile">
          <el-icon><Setting /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体 -->
    <el-container>
      <el-header class="header animate-slide-down">
        <div class="header-left">
          <button class="collapse-btn" @click="isCollapse = !isCollapse" aria-label="折叠菜单">
            <el-icon :size="18">
              <component :is="isCollapse ? Expand : Fold" />
            </el-icon>
          </button>
          <div class="crumbs">
            <span class="crumbs-app">臻选商城</span>
            <el-icon :size="12" color="var(--text-muted)"><ArrowRight /></el-icon>
            <span class="crumbs-page">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-button text @click="router.push('/mall')" class="mall-link">
            <el-icon><Shop /></el-icon>
            <span>回到商城</span>
          </el-button>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="34" class="user-avatar">{{ userStore.username.charAt(0).toUpperCase() }}</el-avatar>
              <div class="user-meta">
                <span class="username">{{ userStore.username }}</span>
                <span class="role-tag">
                  <el-tag size="small" type="primary" effect="plain">卖家</el-tag>
                </span>
              </div>
              <el-icon :size="14" class="dd-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="mall">
                  <el-icon style="margin-right: 6px"><Shop /></el-icon>回到商城
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon style="margin-right: 6px"><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Goods, DataAnalysis, List, User, Fold, Expand, ArrowDown, ArrowRight, Shop, SwitchButton, Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

// 让模板 component 动态切换可用
const ExpandIcon = markRaw(Expand)
const FoldIcon = markRaw(Fold)

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '卖家中心')

async function handleCommand(command) {
  switch (command) {
    case 'mall':
      router.push('/mall')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
        await userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
      break
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      router.push('/login')
    }
  }
})
</script>

<style scoped>
.seller-layout {
  height: 100vh;
  background: var(--bg-deep);
}

/* ===== 侧边栏 ===== */
.aside {
  background:
    linear-gradient(180deg, #f7f8fa 0%, #eef0f4 100%);
  transition: width var(--dur-normal) var(--ease-out-quart);
  overflow: hidden;
  border-right: 1px solid var(--border-color);
  position: relative;
  z-index: 10;
}
.aside::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(360px 240px at 0% 0%, var(--gold-tint), transparent 60%);
  opacity: 0.6;
  pointer-events: none;
}

.logo {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  white-space: nowrap;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
}

.logo-badge {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a1a12;
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  box-shadow: 0 8px 20px -8px var(--gold-glow);
}

.logo-text {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  letter-spacing: 0.02em;
}

/* 菜单覆盖 Element Plus 默认主题 */
.side-menu {
  border-right: none !important;
  background: transparent !important;
  padding: var(--space-sm);
  position: relative;
  z-index: 1;
}
.side-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: var(--radius-md);
  color: var(--text-secondary) !important;
  transition: var(--transition-fast);
}
.side-menu :deep(.el-menu-item:hover) {
  background: var(--bg-tertiary) !important;
  color: var(--text-primary) !important;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, var(--gold-tint-2), var(--gold-tint)) !important;
  color: var(--gold-500) !important;
  font-weight: 600;
  box-shadow: inset 2px 0 0 var(--gold-300);
}
.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}
.side-menu :deep(.el-sub-menu__title) {
  border-radius: var(--radius-md);
}

/* ===== 头部 ===== */
.header {
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  border-bottom: 1px solid var(--border-color);
  height: 68px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.collapse-btn {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition-fast);
}
.collapse-btn:hover {
  color: var(--gold-200);
  border-color: var(--gold-500);
  background: var(--gold-tint);
}

.crumbs {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.crumbs-app {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
  font-weight: 500;
}
.crumbs-page {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  letter-spacing: -0.01em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.mall-link {
  color: var(--text-secondary) !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.mall-link:hover {
  color: var(--gold-200) !important;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px 6px 6px;
  border-radius: var(--radius-pill);
  transition: var(--transition-fast);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
}
.user-info:hover {
  border-color: var(--gold-tint-2);
  background: var(--bg-elevated);
}

.user-avatar {
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500)) !important;
  color: #1a1a12 !important;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 4px 12px -4px var(--gold-glow);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
  gap: 2px;
}
.username {
  font-size: var(--fs-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.role-tag { line-height: 1; }

.dd-arrow {
  color: var(--text-tertiary);
  margin-left: 2px;
}

/* ===== 主内容 ===== */
.main {
  background:
    radial-gradient(1000px 500px at 100% -20%, var(--gold-tint), transparent 60%),
    var(--bg-deep);
  padding: var(--space-xl);
  overflow-y: auto;
}
</style>
