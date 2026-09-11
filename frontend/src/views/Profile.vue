<template>
  <div class="admin-page animate-fade-in-up profile-page">
    <header class="admin-header stagger-1">
      <div>
        <h1 class="admin-title">
          <el-icon :size="22" color="var(--gold-300)"><User /></el-icon>
          个人中心
        </h1>
        <div class="admin-subtitle">管理您的个人信息与账户安全</div>
      </div>
    </header>

    <div class="profile-grid">
      <!-- 左侧：资料卡片 -->
      <section class="panel profile-card stagger-2">
        <div class="profile-avatar-wrap">
          <el-avatar :size="80" class="profile-avatar">
            {{ userStore.username.charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="profile-basic">
            <div class="profile-username">{{ userStore.username }}</div>
            <el-tag :type="userStore.isSeller ? 'warning' : 'info'" effect="light" round size="small" class="role-tag">
              {{ userStore.isSeller ? '卖家' : '买家' }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="90px"
          label-position="right"
          class="profile-form"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" clearable maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :value="0">未知</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存修改</el-button>
            <el-button @click="resetProfile">重置</el-button>
          </el-form-item>
        </el-form>
      </section>

      <!-- 右侧：修改密码 -->
      <section class="panel password-card stagger-3">
        <h2 class="card-title">
          <el-icon :size="18" color="var(--gold-300)"><Lock /></el-icon>
          修改密码
        </h2>
        <p class="card-desc">为了账户安全，建议定期更换密码</p>

        <el-form
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="90px"
          label-position="right"
          class="pwd-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">确认修改</el-button>
            <el-button @click="resetPwd">清空</el-button>
          </el-form-item>
        </el-form>

        <el-alert
          v-if="pwdTip"
          :title="pwdTip"
          type="success"
          :closable="false"
          show-icon
          class="pwd-alert"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { updateProfile, updatePassword } from '../api/user'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const profileFormRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)
const pwdTip = ref('')

const profileForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  gender: 0
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

function fillProfile() {
  const u = userStore.userInfo || {}
  profileForm.nickname = u.nickname || ''
  profileForm.phone = u.phone || ''
  profileForm.email = u.email || ''
  profileForm.gender = u.gender ?? 0
}

async function handleSaveProfile() {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const res = await updateProfile({ ...profileForm })
      userStore.userInfo = { ...userStore.userInfo, ...res.data }
      ElMessage.success('个人信息已更新')
    } catch (e) {
      console.error(e)
    } finally {
      saving.value = false
    }
  })
}

function resetProfile() {
  fillProfile()
  profileFormRef.value?.clearValidate()
}

async function handleChangePassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      await updatePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      pwdTip.value = '密码已更新，下次登录请使用新密码'
      resetPwd()
      setTimeout(() => {
        userStore.logout()
        window.location.href = '/login'
      }, 1500)
    } catch (e) {
      console.error(e)
    } finally {
      changingPwd.value = false
    }
  })
}

function resetPwd() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

onMounted(() => fillProfile())
</script>

<style scoped>
.profile-page { display: flex; flex-direction: column; gap: var(--space-xl); }

.profile-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: var(--space-xl);
  align-items: start;
}

.profile-card, .password-card {
  padding: var(--space-xl);
}

.profile-avatar-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}
.profile-avatar {
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500)) !important;
  color: #1a1a12 !important;
  font-weight: 700;
  font-size: 32px;
}
.profile-basic { display: flex; flex-direction: column; gap: var(--space-sm); }
.profile-username { font-size: var(--fs-lg); font-weight: 700; color: var(--text-emphasis); }
.role-tag { align-self: flex-start; }

.profile-form, .pwd-form { margin-top: var(--space-base); }

.card-title {
  display: flex; align-items: center; gap: var(--space-sm);
  font-size: var(--fs-md); font-weight: 700; color: var(--text-emphasis);
  margin: 0 0 var(--space-xs);
}
.card-desc { margin: 0 0 var(--space-base); font-size: var(--fs-sm); color: var(--text-tertiary); }

.pwd-alert { margin-top: var(--space-base); }

@media (max-width: 900px) {
  .profile-grid { grid-template-columns: 1fr; }
}
</style>
