import request from './request'

/**
 * 用户分页查询（多条件查询）
 * GET /customer/page
 *
 * 请求参数（Query）:
 *   sellerId  - Long         卖家ID（必填，只返回买过该卖家商品的用户）
 *   username  - String       用户名（模糊查询）
 *   phone     - String       手机号（模糊查询）
 *   gender    - Integer      性别: 0-未知 1-男 2-女
 *   level     - Integer      会员等级: 1-3
 *   status    - Integer      状态: 0-禁用 1-启用
 *   startTime - LocalDateTime 注册起始时间
 *   endTime   - LocalDateTime 注册截止时间
 *   current   - Long         当前页，默认1
 *   size      - Long         每页条数，默认10
 *
 * 返回: IPage<Customer>
 *   Customer { id, username, phone, email, gender, level, status,
 *              orderCount, totalAmount, lastOrderTime,
 *              registerTime, createTime, updateTime }
 *
 * ====== 后端实现建议 ======
 * 1. sellerId 必填，通过关联 t_order 表查询下过单的 userId 去重
 * 2. 使用 QueryWrapper 动态条件拼接
 * 3. .like("username", username) / .like("phone", phone)
 * 4. .eq("gender", gender) / .eq("level", level) / .eq("status", status)
 * 5. .ge("register_time", startTime) / .le("register_time", endTime)
 * 6. 逻辑删除已由 MyBatis-Plus @TableLogic 自动处理
 * 7. 返回每个用户的订单数、累计消费金额、最近下单时间（子查询或关联查询）
 */
export function getCustomerPage(params) {
  return request.get('/customer/page', { params })
}

/**
 * 根据ID获取用户
 * GET /customer/{id}
 * 返回: Customer
 */
export function getCustomerById(id) {
  return request.get(`/customer/${id}`)
}

/**
 * 新增用户
 * POST /customer
 * Body: Customer { username, phone, email, gender, level, status }
 * 返回: Boolean
 */
export function addCustomer(data) {
  return request.post('/customer', data)
}

/**
 * 修改用户
 * PUT /customer
 * Body: Customer { id, username, phone, email, gender, level, status }
 * 返回: Boolean
 */
export function updateCustomer(data) {
  return request.put('/customer', data)
}

/**
 * 删除用户（逻辑删除）
 * DELETE /customer/{id}
 * 返回: Boolean
 */
export function deleteCustomer(id) {
  return request.delete(`/customer/${id}`)
}

/**
 * 获取所有用户（用于下拉选择）
 * GET /customer/all
 * 返回: List<Customer>
 */
export function getAllCustomers() {
  return request.get('/customer/all')
}
