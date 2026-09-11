<template>
  <div class="admin-page animate-fade-in-up product-list-page">
    <!-- 页头 -->
    <header class="admin-header stagger-1">
      <div>
        <h1 class="admin-title">
          <el-icon :size="22" color="var(--gold-300)"><Goods /></el-icon>
          商品管理
        </h1>
        <div class="admin-subtitle">维护在售商品信息，控制上架状态与库存</div>
      </div>
    </header>

    <!-- 查询条件 -->
    <section class="panel admin-filter stagger-2">
      <el-form :inline="true" :model="query" class="filter-form">
        <div class="filter-row">
          <div class="filter-field">
            <label>商品名称</label>
            <el-input v-model="query.name" placeholder="请输入关键词" clearable prefix-icon="Search" />
          </div>
          <div class="filter-field">
            <label>商品分类</label>
            <el-cascader
              v-model="query.categoryId"
              :options="categoryOptions"
              :props="{ checkStrictly: true, emitPath: false, value: 'id', label: 'name' }"
              placeholder="全部分类"
              clearable
            />
          </div>
          <div class="filter-field narrow">
            <label>上架状态</label>
            <el-select v-model="query.status" placeholder="全部" clearable>
              <el-option label="已上架" :value="1" />
              <el-option label="已下架" :value="0" />
            </el-select>
          </div>
          <div class="filter-actions-main">
            <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
            <el-button icon="RefreshLeft" @click="handleReset">重置</el-button>
            <el-button link type="primary" @click="showAdvanced = !showAdvanced">
              {{ showAdvanced ? '收起筛选' : '高级筛选' }}
              <el-icon style="margin-left: 2px" :size="12"><ArrowUp v-if="showAdvanced" /><ArrowDown v-else /></el-icon>
            </el-button>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="showAdvanced" class="filter-row filter-adv">
            <div class="filter-field narrow">
              <label>最低价格</label>
              <el-input-number v-model="query.minPrice" :min="0" :precision="2" :controls="false" placeholder="¥" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>最高价格</label>
              <el-input-number v-model="query.maxPrice" :min="0" :precision="2" :controls="false" placeholder="¥" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>最小库存</label>
              <el-input-number v-model="query.minStock" :min="0" :controls="false" placeholder="件" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>最大库存</label>
              <el-input-number v-model="query.maxStock" :min="0" :controls="false" placeholder="件" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>排序字段</label>
              <el-select v-model="query.sortField" placeholder="默认" clearable class="full-w">
                <el-option label="价格" value="price" />
                <el-option label="库存" value="stock" />
                <el-option label="销量" value="sales" />
                <el-option label="创建时间" value="create_time" />
              </el-select>
            </div>
            <div class="filter-field narrow">
              <label>排序方式</label>
              <el-select v-model="query.sortOrder" placeholder="默认" clearable class="full-w">
                <el-option label="升序 ↑" value="asc" />
                <el-option label="降序 ↓" value="desc" />
              </el-select>
            </div>
          </div>
        </el-collapse-transition>

        <div class="filter-footer" v-if="activeFilterCount > 0">
          <div class="filter-stat">
            <el-icon color="var(--gold-300)"><Filter /></el-icon>
            已启用 <b>{{ activeFilterCount }}</b> 个筛选条件
          </div>
        </div>
      </el-form>
    </section>

    <!-- 表格 -->
    <section class="panel admin-table-wrap stagger-3">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" size="default" class="btn-add" @click="handleAdd">
            新增商品
          </el-button>
        </div>
        <div class="toolbar-right">
          <span class="table-hint">共 <b>{{ total }}</b> 条商品</span>
        </div>
      </div>

      <div class="el-table-wrap">
        <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" class="admin-table">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="缩略图" width="84" align="center">
            <template #default="{ row }">
              <el-image :src="getProductImage(row)" fit="cover" class="cell-thumb" lazy>
                <template #error>
                  <div class="thumb-err">{{ row.name?.charAt(0) }}</div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column label="商品信息" min-width="200">
            <template #default="{ row }">
              <div class="cell-product">
                <div class="cell-name" :title="row.name">{{ row.name }}</div>
                <div class="cell-meta">{{ row.categoryName ? '分类：' + row.categoryName : '未分类' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="160" align="right">
            <template #default="{ row }">
              <div class="cell-price-box">
                <div class="price-main">
                  <span class="cell-price">{{ formatMoney(row.price) }}</span>
                  <el-tag v-if="row.originalPrice > row.price" size="small" type="danger" effect="dark" round class="discount-tag">
                    {{ discountPct(row) }}折
                  </el-tag>
                </div>
                <span v-if="row.originalPrice && row.originalPrice > row.price" class="cell-original">{{ formatMoney(row.originalPrice) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="130" align="center">
            <template #default="{ row }">
              <div class="stock-cell">
                <el-tag :type="stockType(row.stock)" effect="light" round size="small" class="stock-tag">
                  {{ row.stock }} 件
                </el-tag>
                <div class="stock-bar">
                  <div class="stock-bar-fill" :class="stockBarClass(row.stock)" :style="{ width: stockPct(row.stock) }"></div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" align="center" sortable />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" round size="small">
                {{ row.status === 1 ? '上架中' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="168" align="center" />
          <el-table-column label="操作" width="240" fixed="right" align="center">
            <template #default="{ row }">
              <div class="cell-actions">
                <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  size="small"
                  :type="row.status === 1 ? 'warning' : 'success'"
                  plain
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="sizes, prev, pager, next, jumper, total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </section>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" width="640px" class="form-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <span class="dot" />
          {{ isEdit ? '编辑商品' : '新增商品' }}
        </div>
      </template>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="86px" class="product-form">
        <div class="form-grid">
          <div class="form-col-span">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </div>
          <div class="form-col-span">
            <el-form-item label="商品分类" prop="categoryId">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryOptions"
                :props="{ checkStrictly: true, emitPath: false, value: 'id', label: 'name' }"
                placeholder="请选择分类"
              />
            </el-form-item>
          </div>
          <el-form-item label="售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" class="full-w" :step="10" />
          </el-form-item>
          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" class="full-w" :step="10" />
          </el-form-item>
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" class="full-w" :step="50" />
          </el-form-item>
          <el-form-item label="上架状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">上架</el-radio>
              <el-radio :label="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
          <div class="form-col-span">
            <el-form-item label="商品图片">
              <div class="img-row">
                <el-upload
                  class="product-uploader"
                  :show-file-list="false"
                  :before-upload="beforeImageUpload"
                  :http-request="handleImageUpload"
                  accept="image/*"
                >
                  <div class="uploader-box">
                    <el-icon :size="24" color="var(--gold-300)"><UploadFilled /></el-icon>
                    <div class="uploader-text">点击上传商品图片</div>
                    <div class="uploader-hint">提交时与商品信息一起保存，JPG/PNG/WEBP ≤ 5MB</div>
                  </div>
                </el-upload>
                <el-image
                  v-if="formThumbSrc"
                  :src="formThumbSrc"
                  fit="cover"
                  class="form-thumb"
                  preview-teleported
                  :preview-src-list="formThumbSrc && formThumbSrc.startsWith('blob:') ? [formThumbSrc] : (formThumbSrc && formThumbSrc.startsWith('http') ? [formThumbSrc] : [])"
                />
                <el-button
                  v-if="pendingImageFile || pendingImagePreview || form.image || originalImageUrl"
                  link
                  type="danger"
                  size="small"
                  class="remove-img-btn"
                  @click="handleRemoveImage"
                >
                  移除图片
                </el-button>
              </div>
            </el-form-item>
          </div>
          <div class="form-col-span">
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品的详细描述" />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="default" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" size="default" @click="handleSubmit">确认提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Search, RefreshLeft, Plus, ArrowUp, ArrowDown, Edit, Delete, Filter, UploadFilled } from '@element-plus/icons-vue'
import { getProductPage, addProduct, updateProduct, deleteProduct, updateProductStatus } from '../api/product'
import { getCategoryTree } from '../api/category'
import { getProductImage } from '../utils/image'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categoryOptions = ref([])
const showAdvanced = ref(false)

const query = reactive({
  name: '',
  categoryId: null,
  minPrice: undefined,
  maxPrice: undefined,
  minStock: undefined,
  maxStock: undefined,
  minSales: undefined,
  maxSales: undefined,
  status: undefined,
  sortField: '',
  sortOrder: '',
  current: 1,
  size: 10
})

const activeFilterCount = computed(() => {
  const keys = ['name', 'categoryId', 'minPrice', 'maxPrice', 'minStock', 'maxStock', 'minSales', 'maxSales', 'status', 'sortField']
  return keys.reduce((n, k) => (query[k] !== '' && query[k] !== null && query[k] !== undefined ? n + 1 : n), 0)
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const pendingImageFile = ref(null)     // 本次提交要一起上传的图片文件
const pendingImagePreview = ref('')   // 本地预览的 ObjectURL
const originalImageUrl = ref('')      // 编辑场景：原本的图片 URL（未换图时保留）
const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  originalPrice: undefined,
  stock: 0,
  status: 1,
  image: '',
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function formatMoney(v) {
  return '¥' + Number(v || 0).toFixed(2)
}
function discountPct(row) {
  if (!row.originalPrice || row.originalPrice <= 0) return 10
  return Math.round((row.price / row.originalPrice) * 10)
}
function stockType(s) {
  if (s < 50) return 'danger'
  if (s < 200) return 'warning'
  return 'success'
}
function stockBarClass(s) {
  if (s < 50) return 'low'
  if (s < 200) return 'mid'
  return 'high'
}
function stockPct(s) {
  return Math.min(100, (s / 1000) * 100) + '%'
}

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    categoryOptions.value = res.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    Object.keys(query).forEach(key => {
      if (query[key] !== '' && query[key] !== null && query[key] !== undefined) params[key] = query[key]
    })
    params.role = 1
    params.userId = userStore.userInfo?.id
    const res = await getProductPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载商品列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  loadData()
}

function handleReset() {
  Object.assign(query, {
    name: '',
    categoryId: null,
    minPrice: undefined,
    maxPrice: undefined,
    minStock: undefined,
    maxStock: undefined,
    minSales: undefined,
    maxSales: undefined,
    status: undefined,
    sortField: '',
    sortOrder: '',
    current: 1,
    size: 10
  })
  loadData()
}

const IMG_MAX_SIZE = 5 * 1024 * 1024 // 5MB
const IMG_ACCEPT_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif']

function beforeImageUpload(file) {
  if (!IMG_ACCEPT_TYPES.includes(file.type)) {
    ElMessage.warning('仅支持 JPG / PNG / WEBP / GIF 格式的图片')
    return false
  }
  if (file.size > IMG_MAX_SIZE) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleImageUpload(options) {
  const { file } = options
  // 先释放旧的预览 URL，避免内存泄漏
  if (pendingImagePreview.value) {
    URL.revokeObjectURL(pendingImagePreview.value)
  }
  pendingImageFile.value = file
  pendingImagePreview.value = URL.createObjectURL(file)
}

function handleRemoveImage() {
  if (pendingImagePreview.value) {
    URL.revokeObjectURL(pendingImagePreview.value)
  }
  pendingImageFile.value = null
  pendingImagePreview.value = ''
  form.image = ''
  originalImageUrl.value = ''
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null, name: '', categoryId: null, price: 0,
    originalPrice: undefined, stock: 0, status: 1, image: '', description: ''
  })
  pendingImageFile.value = null
  pendingImagePreview.value = ''
  originalImageUrl.value = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id, name: row.name, categoryId: row.categoryId,
    price: row.price, originalPrice: row.originalPrice, stock: row.stock,
    status: row.status, image: row.image || '', description: row.description
  })
  pendingImageFile.value = null
  pendingImagePreview.value = ''
  originalImageUrl.value = row.image || ''
  dialogVisible.value = true
}

// 预览图来源优先级：本地待上传 > 已保存的 URL > 根据 name 自动占位
const formThumbSrc = computed(() => {
  if (pendingImagePreview.value) return pendingImagePreview.value
  if (form.image && form.image.startsWith('http')) return form.image
  if (originalImageUrl.value && originalImageUrl.value.startsWith('http')) return originalImageUrl.value
  return getProductImage({ name: form.name || '', categoryId: form.categoryId })
})

async function handleSubmit() {
  try {
    await formRef.value.validate()

    // 如果用户选了新图，就一起上传；没选新图就保留原 image 字段（编辑场景）
    const payload = { ...form }
    if (!pendingImageFile.value && isEdit.value && originalImageUrl.value) {
      payload.image = originalImageUrl.value
    } else if (!pendingImageFile.value && !isEdit.value) {
      // 新增场景无图：image 留空，后端保存即可，前端展示层会用占位图兜底
      payload.image = payload.image || ''
    }

    if (isEdit.value) {
      await updateProduct(payload, pendingImageFile.value || undefined)
      ElMessage.success('商品信息已更新')
    } else {
      await addProduct(payload, pendingImageFile.value || undefined)
      ElMessage.success('商品新增成功')
    }
    dialogVisible.value = false
    if (pendingImagePreview.value) URL.revokeObjectURL(pendingImagePreview.value)
    pendingImageFile.value = null
    pendingImagePreview.value = ''
    loadData()
  } catch (e) {
    if (e && e.message) console.error(e)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '删除确认', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('商品已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleToggleStatus(row) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateProductStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.product-list-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

/* filter */
.filter-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}
.filter-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: var(--space-lg) var(--space-xl);
  align-items: end;
}
.filter-adv {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}
.filter-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  min-width: 0;
}
.filter-field label {
  font-size: var(--fs-sm);
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.02em;
  padding-left: 2px;
}
.filter-field.narrow {
  min-width: 120px;
}
.filter-actions-main {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
  align-self: end;
  padding-bottom: 2px;
}
.filter-footer {
  padding-top: var(--space-md);
  border-top: 1px dashed var(--border-color);
}
.filter-stat {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fs-sm);
  color: var(--text-secondary);
}
.filter-stat b { color: var(--gold-300); font-weight: 700; }
.full-w { width: 100%; }

/* toolbar */
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
  gap: var(--space-md);
}
.btn-add {
  height: 40px;
  padding: 0 22px;
  letter-spacing: 0.04em;
}
.table-hint {
  font-size: var(--fs-sm);
  color: var(--text-muted);
}
.table-hint b {
  color: var(--gold-300);
  font-weight: 700;
  font-size: var(--fs-base);
  font-variant-numeric: tabular-nums;
  padding: 0 4px;
}

/* table */
.el-table-wrap {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
}
.admin-table :deep(.el-table__row) {
  transition: background-color 0.2s ease;
}
.admin-table :deep(.el-table__row:hover) {
  background: var(--gold-tint) !important;
}
.admin-table :deep(.el-table__row:hover .cell-name) {
  color: var(--gold-200);
}
.admin-table :deep(.el-table__row:hover .cell-thumb) {
  border-color: var(--gold-300);
  box-shadow: 0 2px 10px -2px var(--gold-glow);
}

/* table cells */
.cell-thumb {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}
.thumb-err {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12;
  font-weight: 700;
  font-size: 20px;
}
.cell-product {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cell-name {
  font-size: var(--fs-base);
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cell-meta {
  font-size: var(--fs-xs);
  color: var(--text-muted);
}
.cell-price-box {
  display: flex; flex-direction: column; align-items: flex-end; gap: 2px;
}
.price-main {
  display: flex; align-items: center; gap: 6px;
}
.cell-price {
  color: var(--gold-300);
  font-weight: 700;
  font-size: var(--fs-md);
  font-variant-numeric: tabular-nums;
}
.discount-tag {
  transform: scale(0.85);
  padding: 0 4px;
}
.cell-original {
  color: var(--text-muted);
  text-decoration: line-through;
  font-size: var(--fs-xs);
}
.stock-cell {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
}
.stock-tag { min-width: 60px; }
.stock-bar {
  width: 80px; height: 4px;
  background: var(--bg-tertiary);
  border-radius: 2px;
  overflow: hidden;
}
.stock-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.4s ease;
}
.stock-bar-fill.low { background: linear-gradient(90deg, #f56c6c, #f78989); }
.stock-bar-fill.mid { background: linear-gradient(90deg, #e6a23c, #f0c78a); }
.stock-bar-fill.high { background: linear-gradient(90deg, #67c23a, #95d475); }
.cell-muted {
  color: var(--text-muted);
}

.cell-actions {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
  flex-wrap: nowrap;
}

/* pagination */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-lg) 0 0;
}

/* 弹窗 */
.dialog-header {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: var(--fs-lg);
  font-weight: 700;
  color: var(--text-emphasis);
}
.dialog-header .dot {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 var(--space-lg);
}
.form-col-span {
  grid-column: span 2;
}
.img-row {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
}
.flex-1 { flex: 1; }
.form-thumb {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  flex-shrink: 0;
}
.product-uploader :deep(.el-upload) {
  display: block;
}
.uploader-box {
  width: 220px;
  height: 100px;
  border-radius: var(--radius-md);
  border: 1.5px dashed var(--border-color);
  background: var(--bg-elevated);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: all var(--transition-md);
  user-select: none;
}
.uploader-box:hover {
  border-color: var(--gold-300);
  background: rgba(212, 175, 55, 0.06);
}
.uploader-text {
  font-size: var(--fs-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.uploader-hint {
  font-size: var(--fs-xs);
  color: var(--text-muted);
}
.remove-img-btn {
  align-self: flex-start;
  margin-top: 8px;
}
.product-form :deep(.el-form-item) { margin-bottom: var(--space-lg); }

@media (max-width: 1200px) {
  .filter-row { grid-template-columns: repeat(2, minmax(0,1fr)) auto; }
  .filter-adv { grid-template-columns: repeat(3, minmax(0,1fr)); }
}
@media (max-width: 720px) {
  .filter-row { grid-template-columns: 1fr; }
  .filter-adv { grid-template-columns: 1fr; }
  .form-grid { grid-template-columns: 1fr; }
  .form-col-span { grid-column: span 1; }
  .table-toolbar { flex-direction: column; align-items: flex-start; }
}
</style>
