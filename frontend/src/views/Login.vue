<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-side animate-fade-in">
      <div class="brand-inner">
        <div class="logo-badge animate-scale-in stagger-1">
          <el-icon :size="52"><Goods /></el-icon>
        </div>
        <h1 class="brand-title stagger-2 animate-fade-in-up">臻选商城</h1>
        <p class="brand-slogan stagger-3 animate-fade-in-up">甄选好物 · 品质生活</p>

        <ul class="feature-list stagger-4 animate-fade-in-up">
          <li><span class="dot" /><span>正品保证 · 官方渠道</span></li>
          <li><span class="dot" /><span>极速发货 · 全国配送</span></li>
          <li><span class="dot" /><span>贴心售后 · 无忧退换</span></li>
        </ul>
      </div>

      <!-- 背景装饰 -->
      <div class="bg-orbs" aria-hidden="true">
        <div class="orb orb-1" />
        <div class="orb orb-2" />
        <div class="orb orb-3" />
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-side">
      <div class="form-wrap animate-scale-in">
        <header class="form-header animate-fade-in-up stagger-1">
          <h2>欢迎回来</h2>
          <p>登录账号，开启您的购物之旅</p>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @submit.prevent="handleLogin"
          class="animate-fade-in-up stagger-2"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer animate-fade-in-up stagger-3">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册 →</router-link>
        </div>

        <el-divider>测试账号</el-divider>

        <div class="tips-box animate-fade-in-up stagger-4">
          <div class="tip-row">
            <el-tag type="primary" effect="plain" size="small">买家</el-tag>
            <span>buyer / 123456</span>
          </div>
          <div class="tip-row">
            <el-tag size="small">卖家</el-tag>
            <span>seller / 123456</span>
          </div>
          <p class="tip-notice">需后端实现 /auth/login 接口</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect
    if (userStore.isSeller) {
      router.push(redirect || '/admin/dashboard')
    } else {
      router.push(redirect || '/mall')
    }
  } catch (e) {
    if (e.message) console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--bg-deep);
  overflow: hidden;
}

/* ===== 左侧品牌区 ===== */
.brand-side {
  position: relative;
  background:
    radial-gradient(1000px 600px at 20% 10%, var(--gold-tint-2), transparent 60%),
    radial-gradient(800px 500px at 80% 90%, rgba(96,165,250,0.08), transparent 60%),
    linear-gradient(160deg, #f7f8fa 0%, #eef0f4 50%, #e8eaee 100%);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3xl);
  overflow: hidden;
}

.brand-inner {
  max-width: 420px;
  position: relative;
  z-index: 2;
}

.logo-badge {
  width: 104px;
  height: 104px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a1a12;
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  box-shadow:
    0 16px 40px -12px var(--gold-glow),
    inset 0 1px 0 rgba(255,255,255,0.3);
  margin-bottom: var(--space-2xl);
}

.brand-title {
  font-size: var(--fs-4xl);
  font-weight: 800;
  color: var(--text-emphasis);
  letter-spacing: -0.02em;
  margin: 0 0 var(--space-sm);
  background: linear-gradient(135deg, #fff 0%, var(--gold-200) 60%, var(--gold-400) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-slogan {
  font-size: var(--fs-md);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2xl);
  letter-spacing: 0.08em;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  font-size: var(--fs-base);
  color: var(--text-secondary);
}

.feature-list .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  box-shadow: 0 0 0 4px var(--gold-tint);
  flex-shrink: 0;
}

/* 背景装饰球 */
.bg-orbs {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
}
.orb-1 {
  width: 420px; height: 420px;
  top: -120px; left: -80px;
  background: radial-gradient(circle, var(--gold-tint-2), transparent 60%);
  animation: float 14s ease-in-out infinite;
}
.orb-2 {
  width: 520px; height: 520px;
  bottom: -180px; right: -120px;
  background: radial-gradient(circle, rgba(96,165,250,0.14), transparent 60%);
  animation: float 18s ease-in-out infinite reverse;
}
.orb-3 {
  width: 320px; height: 320px;
  top: 50%; left: 50%;
  background: radial-gradient(circle, var(--gold-tint), transparent 60%);
  animation: float 22s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%      { transform: translate(20px, -30px) scale(1.06); }
}

/* ===== 右侧表单区 ===== */
.form-side {
  position: relative;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl);
}
.form-side::before {
  content: "";
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, var(--border-medium), transparent);
}

.form-wrap {
  width: 100%;
  max-width: 420px;
  padding: var(--space-2xl);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-slow);
}
.form-wrap:hover {
  border-color: var(--gold-tint-2);
  box-shadow: var(--shadow-lg), 0 20px 60px -20px var(--gold-glow);
}

.form-header {
  margin-bottom: var(--space-xl);
}
.form-header h2 {
  font-size: var(--fs-2xl);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0 0 var(--space-xs);
  letter-spacing: -0.01em;
}
.form-header p {
  margin: 0;
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: var(--fs-md);
  letter-spacing: 0.1em;
}

.form-footer {
  text-align: center;
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
  margin-top: var(--space-sm);
}
.register-link {
  color: var(--gold-300);
  font-weight: 600;
  margin-left: 4px;
}
.register-link:hover {
  color: var(--gold-100);
  text-shadow: 0 0 12px var(--gold-glow);
}

.tips-box {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}
.tip-row {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  font-size: var(--fs-sm);
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
  padding: 6px var(--space-sm);
  border-radius: var(--radius-sm);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-subtle);
}
.tip-notice {
  margin: var(--space-sm) 0 0;
  font-size: var(--fs-xs);
  color: var(--text-muted);
  text-align: center;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .brand-side {
    display: none;
  }
  .form-side::before { display: none; }
}
</style>
