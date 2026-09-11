import request from './request'

/**
 * 获取购物车列表
 * GET /cart/list
 * 返回: List<CartItem>
 *   CartItem {
 *     id, productId, sellerId, productName, productImage,
 *     price, stock, quantity, selected (0/1)
 *   }
 *
 * ====== 后端实现建议 ======
 * 关联 t_cart 和 t_product 表查询
 * SELECT c.id, c.product_id, p.user_id as sellerId, p.name as productName,
 *        p.price, p.stock, c.quantity, c.selected
 * FROM t_cart c
 * JOIN t_product p ON c.product_id = p.id
 * WHERE c.customer_id = #{当前登录用户ID} AND p.deleted = 0
 */
export function getCartList() {
  return request.get('/cart/list')
}

/**
 * 获取购物车商品数量
 * GET /cart/count
 * 返回: Integer
 */
export function getCartCount() {
  return request.get('/cart/count')
}

/**
 * 添加商品到购物车
 * POST /cart/add
 * Body: { productId, quantity }
 * 返回: Boolean
 *
 * ====== 后端实现建议 ======
 * 1. 先查询购物车是否已有该商品
 * 2. 已有则 quantity += 新数量
 * 3. 没有则新增记录
 */
export function addToCart(data) {
  return request.post('/cart/add', data)
}

/**
 * 修改购物车商品数量
 * PUT /cart/quantity/{id}/{quantity}
 * 返回: Boolean
 */
export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/quantity/${id}/${quantity}`)
}

/**
 * 修改购物车选中状态
 * PUT /cart/select/{id}/{selected}
 * selected: 0-取消选中 1-选中
 * 返回: Boolean
 */
export function updateCartSelected(id, selected) {
  return request.put(`/cart/select/${id}/${selected}`)
}

/**
 * 全选/全不选
 * PUT /cart/select-all/{selected}
 * 返回: Boolean
 */
export function selectAllCart(selected) {
  return request.put(`/cart/select-all/${selected}`)
}

/**
 * 删除购物车商品
 * DELETE /cart/{id}
 * 返回: Boolean
 */
export function deleteCartItem(id) {
  return request.delete(`/cart/${id}`)
}

/**
 * 批量删除购物车商品
 * DELETE /cart/batch
 * Body: { ids: [1, 2, 3] }
 * 返回: Boolean
 */
export function batchDeleteCart(ids) {
  return request.delete('/cart/batch', { data: { ids } })
}
