import request from './request'

/**
 * 获取仪表盘统计数据
 * GET /dashboard/overview
 *
 * 返回: {
 *   totalSales:     BigDecimal  总销售额（已完成订单金额合计）
 *   totalOrders:    Long        订单总数
 *   totalCustomers: Long        用户总数
 *   totalProducts:  Long        商品总数
 * }
 *
 * ====== 后端实现建议 ======
 * 1. totalSales: SELECT SUM(total_amount) FROM t_order WHERE status = 3 (已完成)
 * 2. totalOrders: SELECT COUNT(*) FROM t_order
 * 3. totalCustomers: SELECT COUNT(*) FROM t_customer WHERE deleted = 0
 * 4. totalProducts: SELECT COUNT(*) FROM t_product WHERE deleted = 0
 * 5. 可用 MyBatis-Plus 的 selectCount 或自定义SQL
 */
export function getOverview() {
  return request.get('/dashboard/overview')
}

/**
 * 近7天销售趋势
 * GET /dashboard/sales-trend
 *
 * 返回: [
 *   { date: "2024-01-15", amount: 8999.00, orderCount: 1 }
 * ]
 *
 * ====== 后端实现建议 ======
 * SELECT DATE(create_time) as date,
 *        SUM(total_amount) as amount,
 *        COUNT(*) as orderCount
 * FROM t_order
 * WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
 * GROUP BY DATE(create_time)
 * ORDER BY date
 */
export function getSalesTrend() {
  return request.get('/dashboard/sales-trend')
}

/**
 * 订单状态分布
 * GET /dashboard/order-status
 *
 * 返回: [
 *   { name: "待付款", value: 3 },
 *   { name: "已付款", value: 2 },
 *   { name: "已发货", value: 3 },
 *   { name: "已完成", value: 18 },
 *   { name: "已取消", value: 2 }
 * ]
 *
 * ====== 后端实现建议 ======
 * SELECT status, COUNT(*) as value FROM t_order
 * WHERE deleted = 0
 * GROUP BY status
 * 再在Java中映射状态码到名称
 */
export function getOrderStatusDist() {
  return request.get('/dashboard/order-status')
}
