import request from './request'

/**
 * 用户登录（双Token认证）
 * POST /auth/login
 * Body: { username, password }
 *
 * 返回: {
 *   accessToken:  String  访问令牌（有效期短，如30分钟）
 *   refreshToken: String  刷新令牌（有效期长，如7天）
 * }
 *
 * ====== 后端实现建议 ======
 * 1. 验证用户名密码
 * 2. 生成 accessToken (JWT, 短期) 和 refreshToken (JWT或UUID, 长期)
 * 3. refreshToken 存入 Redis 或数据库，便于校验和吊销
 * 4. 返回双Token
 */
export function login(data) {
  return request.post('/auth/login', data, { headers: { skipAuth: true } })
}

/**
 * 用户注册
 * POST /auth/register
 * Body: { username, password, phone, email }
 * 返回: Boolean
 */
export function register(data) {
  return request.post('/auth/register', data, { headers: { skipAuth: true } })
}

/**
 * 获取当前登录用户信息
 * GET /auth/info
 * 返回: {
 *   id, username, phone, email, gender, role (0-买家 1-卖家),
 *   avatar, nickname, level, status
 * }
 */
export function getUserInfo() {
  return request.get('/auth/info')
}

/**
 * 刷新Token
 * POST /auth/refresh
 * Body: { refreshToken }
 * 返回: { accessToken, refreshToken }
 */
export function refreshToken(data) {
  return request.post('/auth/refresh', data, { headers: { skipAuth: true } })
}

/**
 * 退出登录
 * POST /auth/logout
 * Body: { refreshToken }
 * 返回: Boolean
 * 后端应使 refreshToken 失效
 */
export function logout(data) {
  return request.post('/auth/logout', data)
}
