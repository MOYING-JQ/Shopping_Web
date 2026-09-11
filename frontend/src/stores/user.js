import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAccessToken, getRefreshToken, setTokens, removeTokens, getTokenExpiration } from '../utils/auth'
import { getUserInfo, login as loginApi, register as registerApi, logout as logoutApi, refreshToken as refreshApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getAccessToken() || '')
  const refreshTokenVal = ref(getRefreshToken() || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)
  const isSeller = computed(() => userInfo.value?.role === 1)
  const isBuyer = computed(() => userInfo.value?.role === 0 || userInfo.value?.role === 1)
  const username = computed(() => userInfo.value?.username || '')

  // ===== 定时自动刷新 Token =====
  let refreshTimer = null

  function scheduleRefresh() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }

    const accessToken = token.value
    if (!accessToken) return

    const exp = getTokenExpiration(accessToken)
    if (!exp) return

    const now = Date.now()
    // 提前 10 秒刷新，避免过期后才触发
    const delay = exp - now - 10 * 1000

    if (delay > 0) {
      refreshTimer = setTimeout(async () => {
        try {
          await doRefreshToken()
          scheduleRefresh()
        } catch {
          clearAuth()
        }
      }, delay)
    }
    // delay <= 0 说明 token 已过期或即将过期，交给路由守卫/拦截器处理
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const { accessToken, refreshToken } = res.data
    token.value = accessToken
    refreshTokenVal.value = refreshToken
    setTokens(accessToken, refreshToken)
    await fetchUserInfo()
    scheduleRefresh()
    return res
  }

  async function register(registerForm) {
    return await registerApi(registerForm)
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res.data
  }

  async function doRefreshToken() {
    try {
      const res = await refreshApi({ refreshToken: refreshTokenVal.value })
      const { accessToken, refreshToken } = res.data
      token.value = accessToken
      refreshTokenVal.value = refreshToken
      setTokens(accessToken, refreshToken)
      scheduleRefresh()
      return accessToken
    } catch (e) {
      clearAuth()
      throw e
    }
  }

  async function logout() {
    try {
      await logoutApi({ refreshToken: refreshTokenVal.value })
    } catch (e) {
      // 即使接口失败也清除本地
    }
    clearAuth()
  }

  function clearAuth() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
    token.value = ''
    refreshTokenVal.value = ''
    userInfo.value = null
    removeTokens()
  }

  // 页面加载时如果有 token，立即安排自动刷新
  if (token.value) {
    scheduleRefresh()
  }

  return {
    token, userInfo, isLoggedIn, role, isSeller, isBuyer, username,
    login, register, fetchUserInfo, doRefreshToken, logout, clearAuth,
    scheduleRefresh
  }
})
