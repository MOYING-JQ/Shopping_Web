import request from './request'

/**
 * 买家订单分页查询（我的订单）
 * GET /order/page
 *
 * 请求参数（Query）:
 *   customerId   - Long         买家用户ID（必填，后端按此过滤）
 *   orderNo      - String       订单编号（模糊查询）
 *   status       - Integer      订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
 *   current      - Long         当前页，默认1
 *   size         - Long         每页条数，默认10
 *
 * 返回: IPage<Order>
 *   Order { id, orderNo, customerId, customerName, customerPhone,
 *           sellerId, sellerName,
 *           productId, productName, productImage, price, quantity,
 *           totalAmount, status, remark,
 *           receiverName, receiverPhone, receiverAddress,
 *           createTime, payTime, shipTime, finishTime, updateTime }
 *
 * ====== 后端实现建议 ======
 * 1. 只查 customerId = ? 的订单，不暴露其他买家数据
 * 2. 关联 t_user 查出 sellerName
 * 3. 在 Mapper.xml 中用自定义 SQL + <where> 动态条件
 * 4. 分页可使用 MyBatis-Plus 的 IPage + 自定义SQL
 */
export function getOrderPage(params) {
  return request.get('/order/page', { params })
}

/**
 * 卖家订单分页查询（卖家中心订单管理）
 * GET /order/seller/page
 *
 * 请求参数（Query）:
 *   sellerId     - Long         卖家用户ID（必填，后端按此过滤）
 *   orderNo      - String       订单编号（模糊查询）
 *   customerName - String       买家用户名（模糊查询，需关联t_user表）
 *   status       - Integer      订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
 *   minAmount    - BigDecimal   最小金额
 *   maxAmount    - BigDecimal   最大金额
 *   startTime    - LocalDateTime 起始创建时间
 *   endTime      - LocalDateTime 截止创建时间
 *   sortField    - String       排序字段: totalAmount / createTime
 *   sortOrder    - String       排序方式: asc / desc
 *   current      - Long         当前页，默认1
 *   size         - Long         每页条数，默认10
 *
 * 返回: IPage<Order>（同上，但只含该卖家的订单）
 *
 * ====== 后端实现建议 ======
 * 1. 只查 seller_id = ? 的订单，不暴露其他卖家数据
 * 2. 关联 t_user 查出 customerName、customerPhone（买家信息）
 * 3. 在 Mapper.xml 中用自定义 SQL + <where> 动态条件
 * 4. 分页可使用 MyBatis-Plus 的 IPage + 自定义SQL
 */
export function getSellerOrderPage(params) {
  return request.get('/order/seller/page', { params })
}

/**
 * 根据ID获取订单详情
 * GET /order/{id}
 * 返回: Order（单笔订单对应一个商品，不再使用 t_order_item）
 *   Order {
 *     id, orderNo, customerId, customerName, customerPhone,
 *     sellerId, sellerName,
 *     productId, productName, productImage, price, quantity,
 *     totalAmount, totalQuantity, status, remark,
 *     receiverName, receiverPhone, receiverAddress,
 *     createTime, payTime, updateTime
 *   }
 *
 * ====== 后端实现建议 ======
 * 1. 直接查 t_order 表（已包含商品全部字段）
 * 2. 关联 t_customer 获取 customerName、customerPhone
 * 3. 关联 t_user（卖家表）获取 sellerName
 */
export function getOrderById(id) {
  return request.get(`/order/${id}`)
}

/**
 * 新增订单
 * POST /order
 * Body: {
 *   customerId,
 *   sellerId,                 // 卖家ID（必填，订单归属哪个卖家）
 *   productId,                // 商品ID
 *   productName,              // 商品名称（冗余存储，方便展示）
 *   price,                    // 商品单价
 *   quantity,                 // 购买数量
 *   remark,
 *   receiverName, receiverPhone, receiverAddress
 * }
 * 返回: Long（新创建订单的 ID）
 *
 * ====== 前端调用约定 ======
 * 购物车一次结算多种商品时，前端会按商品逐个调用本接口，
 * 每笔订单只含一个商品，并携带该商品所属卖家的 sellerId。
 * 所有商品信息直接存入 t_order 表，不再使用 t_order_item。
 * 接口返回新订单的 ID，前端据此跳转到支付页。
 *
 * ====== 后端实现建议 ======
 * 1. 生成订单编号（如时间戳+随机数）
 * 2. 根据 price * quantity 计算 totalAmount
 * 3. 直接插入 t_order 表（含 seller_id、product_id、product_name、price、quantity、total_amount 等字段）
 * 4. 扣减商品库存、增加销量
 * 5. 建议加 @Transactional 事务控制
 * 6. 返回新建订单的主键 id（insert 后回传到 data）
 */
export function addOrder(data) {
  return request.post('/order', data)
}

/**
 * 修改订单状态
 * PUT /order/status/{id}/{status}
 * 返回: Boolean
 * 状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
 *
 * ====== 后端实现建议（按状态写入对应时间） ======
 * 1. status=1（已付款）→ 同时设置 pay_time = now()
 * 2. status=2（已发货）→ 同时设置 ship_time = now()
 * 3. status=3（已完成）→ 同时设置 finish_time = now()
 * 4. 其他状态不设置时间字段
 * 5. update_time 由 MyBatis-Plus 自动填充
 */
export function updateOrderStatus(id, status) {
  return request.put(`/order/status/${id}/${status}`)
}

/**
 * 删除订单（逻辑删除）
 * DELETE /order/{id}
 * 返回: Boolean
 */
export function deleteOrder(id) {
  return request.delete(`/order/${id}`)
}

/**
 * 最近订单列表
 * GET /order/recent
 * 参数: limit - Integer 数量，默认5
 * 返回: List<Order>
 */
export function getRecentOrders(limit = 5) {
  return request.get('/order/recent', { params: { limit } })
}
