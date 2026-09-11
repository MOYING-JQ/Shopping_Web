import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, getRefreshToken, removeTokens, setTokens } from '../utils/auth'
import { useUserStore } from '../stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let isRefreshing = false
let pendingQueue = []
let skipAutoRedirect = false

export function setSkipAutoRedirect(val) {
  skipAutoRedirect = val
}

function processQueue(error, token = null) {
  pendingQueue.forEach(({ config, resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      config.headers['Authorization'] = `Bearer ${token}`
      resolve(request(config))
    }
  })
  pendingQueue = []
}

request.interceptors.request.use(
  config => {
    const token = getAccessToken()
    if (token && !config.headers.skipAuth) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  async error => {
    const originalRequest = error.config

    if (error.response) {
      const status = error.response.status

      if (status === 401 && !originalRequest._retry && !originalRequest.headers.skipAuth) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            pendingQueue.push({ config: originalRequest, resolve, reject })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const refreshToken = getRefreshToken()
          if (!refreshToken) {
            throw new Error('no refresh token')
          }

          const res = await axios.post('/api/auth/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefreshToken } = res.data.data

          // 同时更新 localStorage + Pinia store，保持数据一致
          setTokens(accessToken, newRefreshToken)
          try {
            const userStore = useUserStore()
            userStore.token = accessToken
            userStore.refreshTokenVal = newRefreshToken
          } catch (e) { /* store 未初始化时忽略 */ }

          processQueue(null, accessToken)

          originalRequest.headers['Authorization'] = `Bearer ${accessToken}`
          return request(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          removeTokens()
          if (!skipAutoRedirect) {
            // 非路由守卫场景（用户在页面操作时 token 过期）→ 自动跳登录
            ElMessage.warning('登录已过期，请重新登录')
            setTimeout(() => {
              window.location.href = '/login'
            }, 1500)
          }
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      if (status === 404) {
        ElMessage.error('接口不存在，请检查后端是否已实现该接口')
      } else if (status === 403) {
        ElMessage.error('没有权限执行此操作')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else if (status !== 401) {
        ElMessage.error(`请求错误 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查后端是否已启动')
    } else {
      ElMessage.error('无法连接到后端服务，请确保后端运行在 localhost:8080')
    }
    return Promise.reject(error)
  }
)

export default request
