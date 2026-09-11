<template>
  <div class="register-page">
    <div class="register-box animate-scale-in">
      <div class="register-header animate-fade-in-up">
        <el-icon :size="36" class="brand-icon"><Shop /></el-icon>
        <h1>创建账号</h1>
        <p class="register-sub">加入 <span class="brand-mark">GOLD SHOP</span> ，开启精致购物之旅</p>
      </div>

      <div class="role-picker">
        <div class="role-card" :class="{ active: form.role === 0 }" @click="form.role = 0">
          <div class="role-icon"><ElIcon :size="22"><ShoppingBag /></ElIcon></div>
          <div class="role-text">
            <h4>我是买家</h4>
            <p>浏览商品·下单购买·管理订单</p>
          </div>
          <div class="role-check"><span v-if="form.role === 0">✓</span></div>
        </div>
        <div class="role-card" :class="{ active: form.role === 1 }" @click="form.role = 1">
          <div class="role-icon seller"><ElIcon :size="22"><Tools /></ElIcon></div>
          <div class="role-text">
            <h4>我是卖家</h4>
            <p>发布商品·经营店铺·查看数据</p>
          </div>
          <div class="role-check"><span v-if="form.role === 1">✓</span></div>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister">
            创 建 账 号
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录 →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, Shop, ShoppingBag, Tools } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  role: 0
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
    loading.value = true
    const { confirmPassword, ...registerData } = form
    await userStore.register(registerData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    if (e.message) console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  overflow: hidden;
  background:
    radial-gradient(1000px 500px at 10% -10%, rgba(212,175,55,0.08), transparent 60%),
    radial-gradient(900px 600px at 110% 10%, rgba(212,175,55,0.05), transparent 60%),
    linear-gradient(135deg, #f7f8fa 0%, #eef0f4 50%, #e8eaee 100%);
}
.register-page::before,
.register-page::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  pointer-events: none;
}
.register-page::before {
  width: 420px; height: 420px;
  background: radial-gradient(circle, rgba(212,175,55,0.35), transparent 70%);
  top: -120px; left: -80px;
}
.register-page::after {
  width: 520px; height: 520px;
  background: radial-gradient(circle, rgba(160,90,60,0.3), transparent 70%);
  bottom: -180px; right: -120px;
}

.register-box {
  position: relative;
  width: 520px;
  max-width: 100%;
  padding: 44px 44px 36px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(28px) saturate(140%);
  -webkit-backdrop-filter: blur(28px) saturate(140%);
  border: 1px solid rgba(212, 175, 55, 0.25);
  border-radius: 20px;
  box-shadow:
    0 30px 60px -20px rgba(0,0,0,0.12),
    0 0 0 1px rgba(255,255,255,0.5) inset;
  animation: breathe 7s ease-in-out infinite;
}
@keyframes breathe {
  0%,100% { box-shadow: 0 30px 60px -20px rgba(0,0,0,0.12), 0 0 0 1px rgba(255,255,255,0.5) inset, 0 0 40px -20px rgba(212,175,55,0); }
  50% { box-shadow: 0 30px 60px -20px rgba(0,0,0,0.12), 0 0 0 1px rgba(255,255,255,0.5) inset, 0 0 60px -10px rgba(212,175,55,0.18); }
}
.register-box::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(212,175,55,0.55), transparent);
}

.register-header { text-align: center; margin-bottom: 28px; }
.brand-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 64px; height: 64px; border-radius: 18px;
  background:
    radial-gradient(circle at 30% 20%, rgba(255,255,255,0.2), transparent 50%),
    linear-gradient(135deg, rgba(212,175,55,0.22), rgba(212,175,55,0.06));
  border: 1px solid rgba(212,175,55,0.35);
  color: var(--gold-300, #d4af37) !important;
  box-shadow: 0 10px 30px -10px rgba(212,175,55,0.5);
  padding: 12px;
}
.register-header h1 {
  margin: 18px 0 6px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.04em;
  background: linear-gradient(180deg, #1a1a1e 0%, #5a5a68 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.register-sub {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13.5px;
  letter-spacing: 0.02em;
}
.brand-mark {
  color: var(--gold-300, #d4af37);
  font-weight: 700;
  letter-spacing: 0.08em;
}

/* 角色选择卡片 */
.role-picker {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
}
.role-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 1, 0.5, 1);
  overflow: hidden;
}
.role-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(212,175,55,0.15), transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}
.role-card:hover {
  border-color: rgba(212,175,55,0.3);
  transform: translateY(-1px);
}
.role-card.active {
  border-color: rgba(212,175,55,0.55);
  background: rgba(212,175,55,0.07);
  box-shadow: 0 10px 30px -15px rgba(212,175,55,0.45);
}
.role-card.active::after { opacity: 1; }
.role-icon {
  flex: 0 0 40px; width: 40px; height: 40px;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 12px;
  color: var(--text-secondary);
  background: linear-gradient(135deg, #e8eaf0, #dde0e8);
  border: 1px solid var(--border-color);
  position: relative; z-index: 1;
}
.role-icon.seller {
  color: #1a1a12;
  background: linear-gradient(135deg, #f0d98a, #d4af37);
}
.role-text { position: relative; z-index: 1; min-width: 0; }
.role-text h4 {
  margin: 0;
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
}
.role-text p {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: var(--text-tertiary);
  letter-spacing: 0.01em;
  white-space: nowrap;
}
.role-check {
  position: absolute;
  right: 10px; top: 50%; transform: translateY(-50%);
  z-index: 1;
}
.role-check span {
  display: inline-flex; align-items: center; justify-content: center;
  width: 20px; height: 20px; border-radius: 50%;
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  color: #1a1a12;
  font-size: 12px; font-weight: 700;
  box-shadow: 0 4px 12px -2px rgba(212,175,55,0.55);
}

/* form */
.register-form :deep(.el-form-item) { margin-bottom: 18px; }
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 0.12em;
  border-radius: 14px !important;
  background: linear-gradient(135deg, #f5df94 0%, #d4af37 45%, #b8941f 100%) !important;
  border: none !important;
  color: #1a1a12 !important;
  box-shadow: 0 14px 30px -12px rgba(212,175,55,0.55);
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease !important;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px -10px rgba(212,175,55,0.65);
  filter: brightness(1.05);
}
.submit-btn:active { transform: translateY(0); }

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #7e7e8a;
  letter-spacing: 0.02em;
}
.login-link {
  color: var(--gold-300, #d4af37);
  text-decoration: none;
  margin-left: 6px;
  font-weight: 600;
  transition: all 0.25s;
}
.login-link:hover {
  color: #f5df94;
  letter-spacing: 0.06em;
}

:deep(.el-input__wrapper) {
  background-color: var(--bg-tertiary);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  border-radius: 12px;
  padding: 3px 14px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(212,175,55,0.25) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px rgba(212,175,55,0.55) inset,
    0 0 0 4px rgba(212,175,55,0.08);
  transform: translateY(-1px);
}
:deep(.el-input__inner) {
  color: var(--text-primary);
  height: 38px !important;
  font-size: 14.5px !important;
}
:deep(.el-input__inner::placeholder) { color: var(--text-muted); }
:deep(.el-input__prefix) { color: var(--text-tertiary); }

/* 去掉 radio（不再使用），保留交互通用 */
.register-box :deep(.el-input__wrapper) {
  transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1) !important;
}
.register-box:hover {
  box-shadow:
    0 40px 90px -30px rgba(0,0,0,0.75),
    0 0 0 1px rgba(255,255,255,0.02) inset,
    0 0 70px -10px rgba(212,175,55,0.22);
}

@media (max-width: 560px) {
  .register-box { padding: 32px 22px; border-radius: 16px; }
  .role-picker { grid-template-columns: 1fr; }
  .register-header h1 { font-size: 24px; }
}
</style>
