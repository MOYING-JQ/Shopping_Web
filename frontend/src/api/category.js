import request from './request'

/**
 * 获取所有分类（树形结构）
 * GET /category/tree
 * 返回: List<Category> 树形列表
 *   Category { id, name, parentId, sort, status, children: [] }
 */
export function getCategoryTree() {
  return request.get('/category/tree')
}

/**
 * 获取所有分类（扁平列表）
 * GET /category/list
 * 返回: List<Category>
 *   Category { id, name, parentId, sort, status }
 */
export function getCategoryList() {
  return request.get('/category/list')
}
