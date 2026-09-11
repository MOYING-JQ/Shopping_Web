import request from './request'

/**
 * 商品分页查询（复杂多条件查询）
 * GET /product/page
 *
 * 请求参数（Query）:
 *   name        - String   商品名称（模糊查询）
 *   categoryId  - Long     分类ID
 *   minPrice    - BigDecimal  最低价格
 *   maxPrice    - BigDecimal  最高价格
 *   minStock    - Integer     最小库存
 *   maxStock    - Integer     最大库存
 *   minSales    - Integer     最小销量
 *   maxSales    - Integer     最大销量
 *   status      - Integer     状态: 0-下架 1-上架
 *   sortField   - String   排序字段: price / stock / sales / createTime
 *   sortOrder   - String   排序方式: asc / desc
 *   current     - Long     当前页，默认1
 *   size        - Long     每页条数，默认10
 *
 * 返回: IPage<Product>
 *   Product { id, name, categoryId, categoryName, price, originalPrice,
 *            stock, sales, description, status, createTime, updateTime }
 *
 * ====== 后端实现建议 ======
 * 1. 使用 MyBatis-Plus 的 QueryWrapper 进行动态条件拼接
 * 2. 使用 .like() 处理名称模糊查询
 * 3. 使用 .ge() / .le() 处理价格、库存、销量范围
 * 4. 使用 .eq() 处理分类和状态精确匹配
 * 5. 使用 .orderBy() 处理动态排序
 * 6. 使用 IPage + PaginationInnerInterceptor 分页
 * 7. 通过关联查询或自定义SQL获取 categoryName
 */
export function getProductPage(params) {
  return request.get('/product/page', { params })
}

/**
 * 根据ID获取商品详情
 * GET /product/{id}
 * 返回: Product
 */
export function getProductById(id) {
  return request.get(`/product/${id}`)
}

/**
 * 构建商品 multipart FormData（新增/修改共用）
 * @param {Object} data   Product 字段（name/categoryId/price/originalPrice/stock/image/description/status 等）
 * @param {File}   [file] 可选，本次新上传的图片文件
 *
 * 后端建议用 DTO + MultipartFile 一起接收：
 *   public Result<Boolean> add(@ModelAttribute ProductDTO dto,
 *                              @RequestParam(value = "file", required = false) MultipartFile file)
 */
function buildProductFormData(data, file) {
  const fd = new FormData()
  Object.keys(data).forEach(key => {
    const val = data[key]
    if (val === undefined || val === null || val === '') return
    fd.append(key, val)
  })
  if (file) {
    fd.append('file', file)
  }
  return fd
}

/**
 * 新增商品（一次提交：商品信息 + 图片文件）
 * POST /product
 * Content-Type: multipart/form-data
 * Form 字段：
 *   name categoryId price originalPrice stock description status image
 *   file (可选，本次新上传的图片)
 * 返回: Boolean
 */
export function addProduct(data, file) {
  return request.post('/product', buildProductFormData(data, file))
}

/**
 * 修改商品（一次提交：商品信息 + 图片文件）
 * PUT /product
 * Content-Type: multipart/form-data
 * Form 字段：
 *   id name categoryId price originalPrice stock description status image
 *   file (可选，有新文件时才上传)
 * 返回: Boolean
 */
export function updateProduct(data, file) {
  // 注意：部分 Spring 版本对 multipart PUT 支持弱，这里改用 POST + _method=put 兜底兼容性不好
  // 直接走 PUT；后端如有限制，可在 Filter 中把 MultipartResolver 切到 StandardServletMultipartResolver
  return request.put('/product', buildProductFormData(data, file))
}

/**
 * 删除商品（逻辑删除）
 * DELETE /product/{id}
 * 返回: Boolean
 */
export function deleteProduct(id) {
  return request.delete(`/product/${id}`)
}

/**
 * 商品上下架
 * PUT /product/status/{id}/{status}
 * 返回: Boolean
 */
export function updateProductStatus(id, status) {
  return request.put(`/product/status/${id}/${status}`)
}

/**
 * 商品销量排行榜（卖家后台用）
 * GET /product/top-sales
 * 参数: limit - Integer 排行数量，默认10
 * 返回: List<Product>
 *
 * ====== 后端实现建议 ======
 * 使用 QueryWrapper .orderByDesc("sales").last("LIMIT " + limit)
 */
export function getTopSalesProducts(limit = 10) {
  return request.get('/product/top-sales', { params: { limit } })
}

/**
 * 商城热销榜（买家端用，只返回上架商品）
 * GET /product/mall-hot
 * 参数: limit - Integer 排行数量，默认10
 * 返回: List<Product>
 *
 * 与 /product/top-sales 的区别：
 * 1. 只返回 status=1（上架中）的商品
 * 2. 不需要登录鉴权（游客也可看）
 * 3. 可按需增加 categoryId 参数做分类热销
 *
 * ====== 后端实现建议 ======
 * QueryWrapper.eq("status", 1).orderByDesc("sales").last("LIMIT " + limit)
 */
export function getMallHotProducts(limit = 10) {
  return request.get('/product/mall-hot', { params: { limit } })
}
