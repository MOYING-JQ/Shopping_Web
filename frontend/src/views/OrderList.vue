<template>
  <div class="admin-page animate-fade-in-up order-list-page">
    <header class="admin-header stagger-1">
      <div>
        <h1 class="admin-title">
          <el-icon :size="22" color="var(--gold-300)"><Tickets /></el-icon>
          订单管理
        </h1>
        <div class="admin-subtitle">查看全部订单，处理订单状态与售后</div>
      </div>
    </header>

    <section class="panel admin-filter stagger-2">
      <el-form :inline="true" :model="query" class="filter-form">
        <div class="filter-row">
          <div class="filter-field">
            <label>订单编号</label>
            <el-input v-model="query.orderNo" placeholder="输入订单号" clearable prefix-icon="Search" />
          </div>
          <div class="filter-field">
            <label>用户名称</label>
            <el-input v-model="query.customerName" placeholder="关联用户" clearable />
          </div>
          <div class="filter-field narrow">
            <label>订单状态</label>
            <el-select v-model="query.status" placeholder="全部" clearable>
              <el-option label="待付款" :value="0" />
              <el-option label="已付款" :value="1" />
              <el-option label="已发货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
          </div>
          <div class="filter-field filter-wide">
            <label>下单时间</label>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="—"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="[new Date(0,0,0,0,0,0), new Date(0,0,0,23,59,59)]"
              class="full-w"
            />
          </div>
          <div class="filter-actions-main">
            <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
            <el-button icon="RefreshLeft" @click="handleReset">重置</el-button>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="showAdvanced" class="filter-row filter-adv">
            <div class="filter-field narrow">
              <label>最低金额</label>
              <el-input-number v-model="query.minAmount" :min="0" :precision="2" :controls="false" placeholder="¥" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>最高金额</label>
              <el-input-number v-model="query.maxAmount" :min="0" :precision="2" :controls="false" placeholder="¥" class="full-w" />
            </div>
            <div class="filter-field narrow">
              <label>排序字段</label>
              <el-select v-model="query.sortField" placeholder="默认" clearable class="full-w">
                <el-option label="订单金额" value="total_amount" />
                <el-option label="下单时间" value="create_time" />
                <el-option label="商品数量" value="total_quantity" />
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
        <div class="filter-footer d-flex-between">
          <div class="filter-stat" v-if="activeFilterCount > 0">
            <el-icon color="var(--gold-300)"><Filter /></el-icon>
            已启用 <b>{{ activeFilterCount }}</b> 个筛选条件
          </div>
          <el-button link type="primary" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? '收起高级筛选' : '展开高级筛选' }}
            <el-icon style="margin-left: 2px" :size="12"><ArrowUp v-if="showAdvanced" /><ArrowDown v-else /></el-icon>
          </el-button>
        </div>
      </el-form>
    </section>

    <section class="panel admin-table-wrap stagger-3">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" class="btn-add" @click="handleAdd">新增订单</el-button>
          <el-button icon="Download" plain @click="handleExport">导出订单</el-button>
        </div>
        <div class="toolbar-right">
          <div class="status-tabs">
            <div v-for="(t, i) in quickTabs" :key="i"
              class="tab-chip"
              :class="{ active: String(query.status ?? 'all') === String(t.value) }"
              @click="switchTab(t.value)"
            >
              {{ t.label }}
              <em>{{ t.count }}</em>
            </div>
          </div>
        </div>
      </div>

      <div class="el-table-wrap">
        <el-table :data="tableData" stripe v-loading="loading" style="width: 100%" class="admin-table">
          <el-table-column prop="orderNo" label="订单编号" width="180">
            <template #default="{ row }">
              <span class="order-no-text">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="商品信息" min-width="220">
            <template #default="{ row }">
              <div class="cell-product">
                <el-image :src="getOrderProductImage(row)" fit="cover" class="cell-thumb" lazy>
                  <template #error>
                    <div class="thumb-err">{{ row.productName?.charAt(0) }}</div>
                  </template>
                </el-image>
                <div class="cell-prod-info">
                  <div class="cell-name" :title="row.productName">{{ row.productName || '—' }}</div>
                  <div class="cell-meta">
                    <span>单价 {{ formatMoney(row.price) }}</span>
                    <span class="sep">×</span>
                    <span>{{ row.quantity }} 件</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="买家" width="140">
            <template #default="{ row }">
              <div class="cell-user">
                <div class="user-name">{{ row.customerName || '—' }}</div>
                <div class="user-phone" v-if="row.customerPhone">{{ row.customerPhone }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="120" align="right">
            <template #default="{ row }">
              <span class="cell-amount">{{ formatMoney(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalQuantity" label="件数" width="70" align="center" />
          <el-table-column label="订单状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="dark" round size="small">{{ statusMap[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.remark" class="cell-remark">{{ row.remark }}</span>
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="168" align="center" />
          <el-table-column prop="payTime" label="支付时间" width="168" align="center">
            <template #default="{ row }">
              <span v-if="row.payTime">{{ row.payTime }}</span>
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right" align="center">
            <template #default="{ row }">
              <div class="cell-actions">
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button
                  v-if="row.status < 3 && row.status !== 4"
                  size="small"
                  type="success"
                  plain
                  @click="handleStatusChange(row, row.status + 1)"
                >
                  {{ row.status === 0 ? '收款' : row.status === 1 ? '发货' : '完成' }}
                </el-button>
                <el-dropdown v-if="row.status !== 4" @command="cmd => handleStatusChange(row, cmd)" size="small">
                  <el-button size="small" plain>更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="4">取消订单</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
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

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" width="820px" top="6vh" class="form-dialog detail-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <span class="dot" />
          订单详情
        </div>
      </template>
      <el-descriptions :column="2" border v-if="orderDetail" class="info-descs">
        <el-descriptions-item label="订单编号" :label-style="{width:'110px'}">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTagType(orderDetail.status)" effect="dark" round>{{ statusMap[orderDetail.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="买家">{{ orderDetail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ orderDetail.customerPhone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="gold-strong">{{ formatMoney(orderDetail.totalAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="商品数量">{{ orderDetail.totalQuantity }} 件</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ orderDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ orderDetail.payTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ orderDetail.shipTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ orderDetail.finishTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.receiverAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ orderDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单进度条 -->
      <div class="section-subtitle">
        <span />订单进度
      </div>
      <el-steps :active="stepActive(orderDetail?.status)" finish-status="success" align-center class="order-steps">
        <el-step title="已下单" :description="orderDetail?.createTime || ''" />
        <el-step title="已付款" :description="orderDetail?.payTime || '—'" />
        <el-step title="已发货" :description="orderDetail?.shipTime || '—'" />
        <el-step title="已完成" :description="orderDetail?.finishTime || '—'" />
      </el-steps>

      <div class="section-subtitle">
        <span />商品明细
      </div>
      <el-table :data="orderDetail ? [orderDetail] : []" border size="small" class="mini-table">
        <el-table-column label="商品" width="260">
          <template #default="{ row }">
            <div class="cell-product mini">
              <el-image :src="getOrderProductImage(row)" fit="cover" class="cell-thumb small" lazy>
                <template #error>
                  <div class="thumb-err small">{{ row.productName?.charAt(0) }}</div>
                </template>
              </el-image>
              <div class="cell-prod-info">
                <div class="cell-name" :title="row.productName">{{ row.productName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }"><b class="gold-text">{{ formatMoney(row.totalAmount) }}</b></template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增订单 -->
    <el-dialog v-model="addVisible" width="680px" class="form-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <span class="dot" />
          新增订单
        </div>
      </template>
      <el-form :model="addForm" label-width="86px" class="add-form">
        <el-form-item label="下单用户" required>
          <el-select v-model="addForm.customerId" placeholder="搜索并选择用户" filterable class="full-w">
            <el-option v-for="c in customers" :key="c.id"
              :label="`${c.username}${c.phone ? '（' + c.phone + '）' : ''}`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单备注">
          <el-input v-model="addForm.remark" placeholder="订单备注（选填）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="商品明细">
          <div class="add-items">
            <div class="add-item">
              <el-select v-model="addForm.productId" placeholder="选择商品" filterable class="flex-2" @change="onProductChange">
                <el-option v-for="p in products" :key="p.id"
                  :label="`${p.name}（库存${p.stock}，${formatMoney(p.price)}）`"
                  :value="p.id"
                />
              </el-select>
              <el-input-number v-model="addForm.quantity" :min="1" class="flex-1" />
              <div class="subtotal-cell">
                {{ addForm.price ? formatMoney(addForm.price * addForm.quantity) : '—' }}
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="订单合计">
          <div class="add-total">
            <span>应付总额</span>
            <b class="total-big">{{ formatMoney(addTotalAmount) }}</b>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitOrder">确认创建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, Search, RefreshLeft, Plus, View, Delete, ArrowDown, Filter, Download } from '@element-plus/icons-vue'
import { getSellerOrderPage, getOrderById, addOrder, updateOrderStatus, deleteOrder } from '../api/order'
import { getProductPage } from '../api/product'
import { getAllCustomers } from '../api/customer'
import { useUserStore } from '../stores/user'
import { getProductImage } from '../utils/image'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)
const showAdvanced = ref(false)

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }

const query = reactive({
  orderNo: '',
  customerName: '',
  status: undefined,
  minAmount: undefined,
  maxAmount: undefined,
  startTime: undefined,
  endTime: undefined,
  sortField: '',
  sortOrder: '',
  current: 1,
  size: 10
})

const activeFilterCount = computed(() => {
  const keys = ['orderNo','customerName','status','minAmount','maxAmount','startTime','endTime','sortField']
  return keys.reduce((n,k) => (query[k] !== '' && query[k] !== null && query[k] !== undefined ? n + 1 : n), 0)
})

const quickTabs = computed(() => [
  { label: '全部', value: 'all', count: total.value },
  { label: '待付款', value: 0, count: '' },
  { label: '已付款', value: 1, count: '' },
  { label: '已发货', value: 2, count: '' },
  { label: '已完成', value: 3, count: '' }
])

function switchTab(v) {
  query.status = v === 'all' ? undefined : v
  query.current = 1
  loadData()
}

function statusTagType(s) {
  const m = { 0: 'warning', 1: 'primary', 2: '', 3: 'success', 4: 'info' }
  return m[s] || 'info'
}
function stepActive(status) {
  if (status === 4) return 0
  return Number(status) || 0
}
function formatMoney(v) {
  return '¥' + Number(v || 0).toFixed(2)
}
function getOrderProductImage(order) {
  if (order.productImage && order.productImage.startsWith('http')) return order.productImage
  return getProductImage({ name: order.productName })
}

async function loadData() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      query.startTime = dateRange.value[0]
      query.endTime = dateRange.value[1]
    } else {
      query.startTime = query.endTime = undefined
    }
    const params = { sellerId: userStore.userInfo?.id }
    Object.keys(query).forEach(k => {
      if (query[k] !== '' && query[k] !== null && query[k] !== undefined) params[k] = query[k]
    })
    const res = await getSellerOrderPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载订单列表失败', e)
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.current = 1; loadData() }
function handleReset() {
  dateRange.value = null
  Object.assign(query, {
    orderNo:'', customerName:'', status:undefined,
    minAmount:undefined, maxAmount:undefined,
    startTime:undefined, endTime:undefined,
    sortField:'', sortOrder:'', current:1, size:10
  })
  loadData()
}
function handleExport() {
  ElMessage.info('正在准备导出数据…')
}

/* 详情 */
const detailVisible = ref(false)
const orderDetail = ref(null)
async function handleDetail(row) {
  try {
    const res = await getOrderById(row.id)
    orderDetail.value = res.data
    detailVisible.value = true
  } catch (e) { console.error(e) }
}

/* 状态切换 */
async function handleStatusChange(row, status) {
  try {
    await updateOrderStatus(row.id, status)
    ElMessage.success(`状态已更新：${statusMap[status]}`)
    loadData()
  } catch (e) { console.error(e) }
}

/* 删除 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除订单「${row.orderNo}」吗？`, '删除确认', { type: 'warning' })
    await deleteOrder(row.id)
    ElMessage.success('订单已删除')
    loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

/* 新增订单 */
const addVisible = ref(false)
const customers = ref([])
const products = ref([])
const addForm = reactive({
  customerId: null,
  remark: '',
  productId: null,
  productName: '',
  quantity: 1,
  price: 0
})
const addTotalAmount = computed(() =>
  (Number(addForm.price) || 0) * (addForm.quantity || 0)
)
function onProductChange(pid) {
  const p = products.value.find(x => x.id === pid)
  if (p) {
    addForm.price = Number(p.price)
    addForm.productName = p.name
  } else {
    addForm.price = 0
    addForm.productName = ''
  }
}
async function handleAdd() {
  addForm.customerId = null
  addForm.remark = ''
  addForm.productId = null
  addForm.productName = ''
  addForm.quantity = 1
  addForm.price = 0
  addVisible.value = true
  try {
    const [cRes, pRes] = await Promise.all([getAllCustomers(), getProductPage({ current: 1, size: 200 })])
    customers.value = cRes.data || []
    products.value = pRes.data?.records || []
  } catch (e) { console.error(e) }
}
async function handleSubmitOrder() {
  if (!addForm.customerId) { ElMessage.warning('请选择下单用户'); return }
  if (!addForm.productId) { ElMessage.warning('请选择商品'); return }
  try {
    await addOrder({
      customerId: addForm.customerId,
      sellerId: userStore.userInfo?.id,
      productId: addForm.productId,
      productName: addForm.productName,
      price: addForm.price,
      quantity: addForm.quantity,
      remark: addForm.remark
    })
    ElMessage.success('订单创建成功')
    addVisible.value = false
    loadData()
  } catch (e) { console.error(e) }
}

onMounted(() => loadData())
</script>

<style scoped>
.order-list-page { display: flex; flex-direction: column; gap: var(--space-xl); }

/* filter */
.filter-form { display: flex; flex-direction: column; gap: var(--space-lg); }
.filter-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(0, 1.8fr) auto;
  gap: var(--space-lg) var(--space-xl);
  align-items: end;
}
.filter-adv { grid-template-columns: repeat(4, minmax(0, 1fr)); }
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
.filter-field.narrow { min-width: 120px; }
.filter-field.filter-wide { grid-column: span 1; }
.filter-field.filter-wide :deep(.el-date-editor),
.filter-field.filter-wide :deep(.el-range-editor) {
  min-width: 0;
  width: 100%;
}
.filter-field.filter-wide :deep(.el-range-input) {
  min-width: 0;
  width: 100%;
}
.filter-field.filter-wide :deep(.el-range-separator) {
  padding: 0 4px;
}

.filter-actions-main {
  display: flex; align-items: center; gap: var(--space-sm); align-self: end; flex-wrap: wrap;
  padding-bottom: 2px;
}

.filter-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-md);
  border-top: 1px dashed var(--border-color);
  gap: var(--space-md);
}
.d-flex-between { display: flex; justify-content: space-between; align-items: center; }
.filter-stat {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: var(--fs-sm); color: var(--text-secondary);
}
.filter-stat b { color: var(--gold-300); font-weight: 700; }
.full-w { width: 100%; }

/* toolbar */
.table-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--space-lg); gap: var(--space-md); flex-wrap: wrap;
}
.toolbar-left { display: flex; gap: var(--space-sm); align-items: center; }
.btn-add { height: 40px; padding: 0 22px; letter-spacing: 0.04em; }

.status-tabs {
  display: inline-flex;
  gap: 2px;
  background: var(--bg-tertiary);
  padding: 3px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--border-color);
}
.tab-chip {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: var(--fs-sm); color: var(--text-secondary);
  padding: 6px 14px; border-radius: var(--radius-pill);
  cursor: pointer; transition: var(--transition-fast);
}
.tab-chip em {
  font-style: normal; font-size: var(--fs-xs); color: var(--text-muted);
  background: var(--bg-secondary); border-radius: 10px; padding: 0 6px;
}
.tab-chip:hover { color: var(--gold-200); }
.tab-chip.active {
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12; font-weight: 600;
  box-shadow: 0 6px 16px -6px var(--gold-glow);
}
.tab-chip.active em { background: rgba(255,255,255,0.25); color: #1a1a12; }

/* table */
.el-table-wrap {
  border-radius: var(--radius-md); overflow: hidden;
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
.order-no-text {
  font-family: var(--font-mono);
  font-size: var(--fs-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.cell-product {
  display: flex; align-items: center; gap: var(--space-sm);
}
.cell-product.mini { gap: var(--space-xs); }
.cell-thumb {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  border: 1px solid var(--border-color); flex-shrink: 0;
}
.cell-thumb.small { width: 40px; height: 40px; }
.thumb-err {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12; font-weight: 700; font-size: 18px;
}
.thumb-err.small { width: 40px; height: 40px; font-size: 16px; }
.cell-prod-info { min-width: 0; flex: 1; }
.cell-name {
  font-size: var(--fs-base); font-weight: 600; color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.cell-meta {
  font-size: var(--fs-xs); color: var(--text-muted); margin-top: 2px;
  display: flex; align-items: center; gap: 4px;
}
.cell-meta .sep { color: var(--text-muted); }
.cell-user { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-size: var(--fs-base); color: var(--text-primary); font-weight: 500; }
.user-phone { font-size: var(--fs-xs); color: var(--text-muted); font-family: var(--font-mono); }
.cell-amount {
  color: var(--gold-300); font-weight: 700; font-size: var(--fs-md); font-variant-numeric: tabular-nums;
}
.cell-remark {
  display: inline-block; padding: 2px 8px;
  background: var(--bg-tertiary); border: 1px solid var(--border-color);
  border-radius: var(--radius-pill); font-size: var(--fs-xs); color: var(--text-secondary);
  max-width: 100%; overflow: hidden; text-overflow: ellipsis;
}
.cell-muted { color: var(--text-muted); }
.cell-actions {
  display: inline-flex; align-items: center; gap: var(--space-xs); flex-wrap: nowrap;
}
.cell-actions .el-dropdown { display: inline-flex; }
.pagination-bar { display: flex; justify-content: flex-end; padding: var(--space-lg) 0 0; }

/* dialog */
.dialog-header {
  display: inline-flex; align-items: center; gap: 10px;
  font-size: var(--fs-lg); font-weight: 700; color: var(--text-emphasis);
}
.dialog-header .dot {
  width: 4px; height: 18px; border-radius: 2px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
}
.dialog-footer { display: flex; justify-content: flex-end; gap: var(--space-sm); }
.detail-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

.gold-strong { color: var(--gold-300); font-weight: 700; font-size: var(--fs-md); }
.gold-text { color: var(--gold-300); font-weight: 700; }

.section-subtitle {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: var(--fs-md); font-weight: 600; color: var(--text-emphasis);
  margin: var(--space-xl) 0 var(--space-md);
}
.section-subtitle span {
  width: 3px; height: 14px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
  border-radius: 2px;
}
.info-descs :deep(.el-descriptions__label) {
  background: var(--bg-tertiary) !important; color: var(--text-secondary) !important;
  border-color: var(--border-color) !important;
}
.info-descs :deep(.el-descriptions__content) {
  background: var(--bg-secondary) !important; color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}
.mini-table :deep(.el-table th) {
  background: var(--bg-tertiary) !important; color: var(--text-secondary) !important; border-color: var(--border-color) !important;
}
.mini-table :deep(.el-table td) {
  background: var(--bg-secondary) !important; color: var(--text-primary) !important; border-color: var(--border-color) !important;
}

/* 订单进度条 */
.order-steps { margin: var(--space-md) 0 var(--space-xl); }
.order-steps :deep(.el-step__title) { color: var(--text-secondary); }
.order-steps :deep(.el-step__description) { color: var(--text-muted); font-size: var(--fs-xs); }
.order-steps :deep(.el-step.is-process .el-step__title) { color: var(--gold-300); }
.order-steps :deep(.el-step__head.is-process) { color: var(--gold-300); border-color: var(--gold-300); }
.order-steps :deep(.el-step__head.is-success) { color: var(--gold-300); border-color: var(--gold-300); }

/* 新增订单表单 */
.add-form :deep(.el-form-item) { margin-bottom: var(--space-lg); }
.add-items { display: flex; flex-direction: column; gap: var(--space-sm); }
.add-item {
  display: grid;
  grid-template-columns: 1fr 120px 100px 36px;
  gap: var(--space-sm);
  align-items: center;
  padding: var(--space-sm);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}
.add-item .flex-2 { width: 100%; }
.add-item .flex-1 { width: 100%; }
.subtotal-cell {
  text-align: right; font-size: var(--fs-sm);
  color: var(--gold-300); font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.btn-add-item { margin-top: var(--space-xs); align-self: flex-start; }
.add-total {
  display: flex; align-items: baseline; gap: 10px;
  padding: var(--space-md) var(--space-lg);
  background: var(--gold-tint); border: 1px solid var(--gold-tint-2);
  border-radius: var(--radius);
}
.add-total span { font-size: var(--fs-base); color: var(--text-secondary); font-weight: 500; }
.total-big {
  font-size: 26px; font-weight: 800; color: var(--gold-300);
  letter-spacing: -0.02em; line-height: 1;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 18px var(--gold-glow);
}

@media (max-width: 1200px) {
  .filter-row { grid-template-columns: repeat(2, minmax(0,1fr)) auto; }
  .filter-adv { grid-template-columns: repeat(2, minmax(0,1fr)); }
  .status-tabs { order: -1; width: 100%; }
  .table-toolbar { align-items: flex-start; }
}
@media (max-width: 720px) {
  .filter-row, .filter-adv { grid-template-columns: 1fr; }
  .add-item { grid-template-columns: 1fr 1fr; grid-auto-rows: auto; }
  .add-item .subtotal-cell,
  .add-item > :last-child { grid-column: span 2; }
}
</style>
