import request from './request'

/**
 * 修改个人信息
 * PUT /auth/profile
 * Body: { nickname, phone, email, gender, avatar }
 * 返回: 更新后的用户信息
 */
export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

/**
 * 修改密码
 * PUT /auth/password
 * Body: { oldPassword, newPassword }
 * 返回: Boolean
 */
export function updatePassword(data) {
  return request.put('/auth/password', data)
}
