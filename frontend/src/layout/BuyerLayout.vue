<template>
  <div class="buyer-layout">
    <!-- 顶部导航栏 -->
    <header class="buyer-header animate-slide-down">
      <div class="container header-inner">
        <div class="logo-area" @click="router.push('/mall')">
          <div class="logo-icon">
            <el-icon :size="20"><Goods /></el-icon>
          </div>
          <div class="logo-meta">
            <span class="logo-text">优选商城</span>
            <span class="logo-sub">Elegant Marketplace</span>
          </div>
        </div>

        <div class="search-area">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索心仪商品..."
            clearable
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon :size="16" style="color: var(--text-tertiary)"><Search /></el-icon>
            </template>
            <template #append>
              <el-button :icon="Search" @click="handleSearch" class="search-btn">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="nav-area">
          <template v-if="userStore.isLoggedIn">
            <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99" class="nav-badge">
              <div class="nav-item" @click="router.push('/cart')">
                <el-icon :size="18"><ShoppingCart /></el-icon>
                <span class="nav-text">购物车</span>
              </div>
            </el-badge>

            <div class="nav-item" @click="router.push('/orders')">
              <el-icon :size="18"><List /></el-icon>
              <span class="nav-text">订单</span>
            </div>

            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-chip">
                <el-avatar :size="30" class="user-avatar">{{ userStore.username.charAt(0).toUpperCase() }}</el-avatar>
                <div class="user-meta">
                  <span class="username">{{ userStore.username }}</span>
                  <span class="user-role">{{ userStore.role === 1 ? '卖家' : '买家' }}</span>
                </div>
                <el-icon :size="14" style="color: var(--text-tertiary)"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="cart">购物车</el-dropdown-item>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isSeller" command="seller" divided>卖家中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 分类栏 -->
    <nav class="category-bar">
      <div class="container category-inner">
        <span
          class="category-chip"
          :class="{ active: !activeCategory }"
          @click="selectCategory(null)"
        >
          <el-icon><Goods /></el-icon>
          全部商品
        </span>
        <span
          v-for="(cat, i) in categories"
          :key="cat.id"
          class="category-chip"
          :class="{ active: activeCategory === cat.id }"
          :style="{ animationDelay: (i * 0.02) + 's' }"
          @click="selectCategory(cat.id)"
        >{{ cat.name }}</span>
      </div>
    </nav>

    <!-- 内容区 -->
    <main class="buyer-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" @update-cart="loadCartCount" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 -->
    <footer class="buyer-footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <div class="logo-icon" style="width:34px;height:34px;">
            <el-icon :size="16"><Goods /></el-icon>
          </div>
          <div>
            <div class="footer-title">优选商城</div>
            <div class="footer-sub">Spring Boot + MyBatis-Plus 后端练习项目</div>
          </div>
        </div>
        <div class="footer-links">
          <span>关于我们</span>
          <span>联系客服</span>
          <span>商家入驻</span>
          <span>隐私政策</span>
        </div>
        <div class="footer-copy">© 2026 优选商城 · All rights reserved.</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, provide, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Search, ShoppingCart, List, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getCategoryList } from '../api/category'
import { getCartCount } from '../api/cart'
import bus from '../utils/bus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const activeCategory = ref(null)
const categories = ref([])
const cartCount = ref(0)

const searchKeywordProvider = ref('')
const categoryProvider = ref(null)

provide('searchKeyword', searchKeywordProvider)
provide('activeCategory', categoryProvider)

function handleSearch() {
  searchKeywordProvider.value = searchKeyword.value
  if (route.path !== '/mall') router.push('/mall')
}

function selectCategory(catId) {
  activeCategory.value = catId
  categoryProvider.value = catId
  if (route.path !== '/mall') router.push('/mall')
}

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadCartCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getCartCount()
    cartCount.value = res.data || 0
  } catch (e) { console.error(e) }
}

async function handleCommand(command) {
  switch (command) {
    case 'orders': router.push('/orders'); break
    case 'cart':   router.push('/cart'); break
    case 'profile': router.push('/profile'); break
    case 'seller': router.push('/admin/dashboard'); break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
        await userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (e) { if (e !== 'cancel') console.error(e) }
      break
  }
}

bus.on('cart:update', loadCartCount)

watch(() => userStore.isLoggedIn, (v) => {
  if (v) loadCartCount()
  else cartCount.value = 0
})

onUnmounted(() => bus.off('cart:update', loadCartCount))

onMounted(async () => {
  loadCategories()
  if (userStore.isLoggedIn) {
    if (!userStore.userInfo) {
      try { await userStore.fetchUserInfo() } catch (e) { console.error(e) }
    }
    loadCartCount()
  }
})
</script>

<style scoped>
.buyer-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-deep);
}

/* ===== 顶部导航栏 ===== */
.buyer-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: saturate(180%) blur(14px);
  -webkit-backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 12px -6px rgba(0,0,0,0.08);
}

.header-inner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  height: 72px;
  gap: var(--space-2xl);
}

/* Logo 区 */
.logo-area {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  cursor: pointer;
  flex-shrink: 0;
}
.logo-icon {
  width: 40px; height: 40px;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 14px -6px var(--gold-glow);
  transition: var(--transition-fast);
}
.logo-area:hover .logo-icon {
  transform: translateY(-1px) rotate(-4deg);
  box-shadow: 0 10px 20px -8px var(--gold-glow);
}
.logo-meta { display: flex; flex-direction: column; line-height: 1.1; }
.logo-text {
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--gold-50), var(--gold-300));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.01em;
}
.logo-sub {
  font-size: 10px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.22em;
  margin-top: 2px;
}

/* 搜索区 */
.search-area { max-width: 560px; width: 100%; }
.search-area :deep(.el-input__wrapper) { height: 44px; }
.search-btn {
  height: 36px;
  line-height: 36px;
  margin-top: -4px;
}

/* 导航区 */
.nav-area {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition-fast);
  font-size: var(--fs-sm);
}
.nav-item:hover {
  background: var(--gold-tint);
  color: var(--gold-500);
}
.nav-badge :deep(.el-badge__content) { top: 4px; right: 4px; }

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 6px 6px 6px 12px;
  border-radius: var(--radius-pill);
  cursor: pointer;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  transition: var(--transition-fast);
}
.user-chip:hover {
  border-color: var(--gold-500);
  background: var(--bg-tertiary);
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.username {
  font-size: var(--fs-sm);
  color: var(--text-primary);
  font-weight: 600;
}
.user-role {
  font-size: 10px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-top: 2px;
}

/* ===== 分类栏 ===== */
.category-bar {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 72px;
  z-index: 90;
}
.category-inner {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  height: 56px;
  overflow-x: auto;
  scrollbar-width: none;
}
.category-inner::-webkit-scrollbar { display: none; }

.category-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 15px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-sm);
  color: var(--text-secondary);
  cursor: pointer;
  white-space: nowrap;
  border: 1px solid transparent;
  background: var(--bg-tertiary);
  transition: var(--transition-fast);
  animation: fadeInUp 0.4s var(--ease-out-quart) both;
}
.category-chip .el-icon { font-size: 14px; }
.category-chip:hover {
  color: var(--gold-500);
  background: var(--bg-elevated);
  border-color: var(--border-medium);
}
.category-chip.active {
  background: linear-gradient(135deg, var(--gold-300), var(--gold));
  color: #1a1a12;
  border-color: transparent;
  font-weight: 700;
  box-shadow: 0 6px 14px -6px var(--gold-glow);
}

/* ===== 内容区 ===== */
.buyer-main {
  flex: 1;
  width: 100%;
}

/* ===== 底部 ===== */
.buyer-footer {
  margin-top: var(--space-3xl);
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  color: var(--text-tertiary);
  font-size: var(--fs-xs);
  padding: var(--space-2xl) 0 var(--space-xl);
}
.footer-inner {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  align-items: center;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}
.footer-title {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
}
.footer-sub {
  font-size: var(--fs-xs);
  color: var(--text-tertiary);
  margin-top: 2px;
}
.footer-links {
  display: flex;
  gap: var(--space-xl);
  color: var(--text-secondary);
}
.footer-links span {
  cursor: pointer;
  transition: var(--transition-fast);
}
.footer-links span:hover { color: var(--gold-200); }
.footer-copy {
  color: var(--text-muted);
  font-size: 11px;
  letter-spacing: 0.04em;
}

/* 响应式 */
@media (max-width: 900px) {
  .header-inner {
    grid-template-columns: auto auto;
    grid-template-rows: auto auto;
    height: auto;
    padding: var(--space-base) var(--space-lg);
    gap: var(--space-base);
  }
  .search-area { grid-column: 1 / -1; order: 3; max-width: 100%; }
  .logo-sub { display: none; }
  .nav-text { display: none; }
  .category-inner { height: 48px; }
}
</style>
