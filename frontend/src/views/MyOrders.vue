<template>
  <div class="page-wrap my-orders-page animate-fade-in-up">
    <header class="page-header">
      <div>
        <h1 class="page-title">
          <el-icon :size="22" color="var(--gold-300)"><Tickets /></el-icon>
          我的订单
        </h1>
        <div class="page-subtitle">追踪您的所有订单状态，随时查看物流和历史交易</div>
      </div>
    </header>

    <!-- 状态 Tab -->
    <section class="panel orders-tabs-panel stagger-1">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="orders-tabs" stretch>
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane name="0">
          <template #label>待付款
            <el-badge v-if="statusCount[0] > 0" :value="statusCount[0]" class="tab-badge" :max="99" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已付款" name="1" />
        <el-tab-pane label="已发货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>
    </section>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-area stagger-2">
      <div v-if="orders.length > 0" class="order-list">
        <article v-for="(order, idx) in orders" :key="order.id" class="panel order-card" :style="{ animationDelay: (idx * 40) + 'ms' }">
          <div class="order-header">
            <div class="order-info">
              <span class="order-meta">
                <el-icon :size="13"><Clock /></el-icon>
                {{ order.createTime }}
              </span>
              <span class="order-no">
                <span class="order-no-label">订单号</span>{{ order.orderNo }}
              </span>
            </div>
            <div class="order-status-area">
              <el-tag :type="statusTagType(order.status)" effect="dark" round size="small">{{ statusMap[order.status] }}</el-tag>
            </div>
          </div>

          <div class="order-items">
            <div class="order-item">
              <el-image :src="getOrderProductImage(order)" fit="cover" class="item-thumb" lazy>
                <template #error>
                  <div class="thumb-err">{{ order.productName?.charAt(0) }}</div>
                </template>
              </el-image>
              <div class="item-info">
                <div class="item-name">{{ order.productName }}</div>
                <div class="item-price">{{ formatPrice(order.price) }} × {{ order.quantity }}</div>
              </div>
              <div class="item-subtotal">{{ formatPrice(order.totalAmount) }}</div>
            </div>
          </div>

          <div class="order-footer">
            <div class="remark-area">
              <div v-if="order.remark" class="order-remark">
                <el-icon :size="12"><ChatLineRound /></el-icon>
                {{ order.remark }}
              </div>
            </div>
            <div class="footer-right">
              <div class="order-total">
                <span class="total-label">共 <b>{{ order.totalQuantity }}</b> 件 &nbsp;|&nbsp; 合计</span>
                <span class="total-amount">{{ formatPrice(order.totalAmount) }}</span>
              </div>
              <div class="order-actions">
                <el-button size="default" @click="showDetail(order)">查看详情</el-button>
                <el-button v-if="order.status === 0" size="default" @click="handleCancel(order)">取消订单</el-button>
                <el-button v-if="order.status === 2" size="default" type="success" @click="handleConfirm(order)">确认收货</el-button>
                <el-button v-if="order.status === 0" size="default" type="primary" class="btn-pay" @click="handlePay(order)">立即付款</el-button>
              </div>
            </div>
          </div>
        </article>
      </div>
      <div v-else class="empty-wrap">
        <el-empty description="暂无订单，快去商城逛逛吧~" :image-size="120">
          <el-button type="primary" round icon="ShoppingCart" @click="$router.push('/mall')">前往商城</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar stagger-3" v-if="total > 0">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="total"
        :pager-count="7"
        layout="sizes, prev, pager, next, jumper, total"
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="720px" class="detail-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <span class="dot" />
          订单详情
        </div>
      </template>
      <el-descriptions :column="2" border v-if="currentOrder" class="info-descs">
        <el-descriptions-item label="订单编号" :label-style="{width:'110px'}">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTagType(currentOrder.status)" effect="dark" round>{{ statusMap[currentOrder.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单进度条 -->
      <div class="section-subtitle">
        <span />订单进度
      </div>
      <el-steps :active="stepActive(currentOrder?.status)" finish-status="success" align-center class="order-steps">
        <el-step title="已下单" :description="currentOrder?.createTime || ''" />
        <el-step title="已付款" :description="currentOrder?.payTime || '—'" />
        <el-step title="已发货" :description="currentOrder?.shipTime || '—'" />
        <el-step title="已完成" :description="currentOrder?.finishTime || '—'" />
      </el-steps>

      <div class="section-subtitle">
        <span />商品明细
      </div>
      <el-table :data="currentOrder ? [currentOrder] : []" border size="small" class="mini-table">
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">
            <b class="gold-text">{{ formatPrice(row.totalAmount) }}</b>
          </template>
        </el-table-column>
      </el-table>

      <div class="detail-total" v-if="currentOrder">
        <span>订单总额</span>
        <b class="total-amount-big">{{ formatPrice(currentOrder.totalAmount) }}</b>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, Clock, ChatLineRound } from '@element-plus/icons-vue'
import { getOrderPage, getOrderById, updateOrderStatus } from '../api/order'
import { useUserStore } from '../stores/user'
import { getProductImage } from '../utils/image'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const activeTab = ref('all')
const statusCount = ref({ 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 })
const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }

const page = reactive({
  current: 1,
  size: 10
})

function statusTagType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: '', 3: 'success', 4: 'info' }
  return map[status] || 'info'
}

function formatPrice(val) {
  return '¥' + Number(val || 0).toFixed(2)
}

function getOrderProductImage(order) {
  if (order.productImage && order.productImage.startsWith('http')) {
    return order.productImage
  }
  return getProductImage({ name: order.productName, categoryId: order.categoryId })
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      customerId: userStore.userInfo?.id,
      current: page.current,
      size: page.size,
      sortField: 'create_time',
      sortOrder: 'desc'
    }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === null || params[key] === '') {
        delete params[key]
      }
    })
    const res = await getOrderPage(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.current = 1
  loadData()
}

function handleSizeChange() {
  page.current = 1
  loadData()
}

async function showDetail(order) {
  try {
    const res = await getOrderById(order.id)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

function stepActive(status) {
  // 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
  if (status === 4) return 0
  return Number(status) || 0
}

async function handlePay(order) {
  router.push({ path: '/payment', query: { orderIds: order.id } })
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(`确定取消订单「${order.orderNo}」吗？`, '提示', { type: 'warning' })
    await updateOrderStatus(order.id, 4)
    ElMessage.success('订单已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm(`确认已收到商品吗？`, '确认收货', { type: 'info' })
    await updateOrderStatus(order.id, 3)
    ElMessage.success('已确认收货')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  loadData()
})
</script>

<style scoped>
.my-orders-page {
  min-height: 520px;
}

.orders-tabs-panel {
  padding: var(--space-md) var(--space-xl) 0;
  margin-bottom: var(--space-lg);
}
.orders-tabs :deep(.el-tabs__item) {
  padding: 0 18px;
  height: 46px;
  line-height: 46px;
  font-size: var(--fs-base);
  letter-spacing: 0.02em;
  color: var(--text-secondary);
}
.orders-tabs :deep(.el-tabs__item.is-active) {
  color: var(--gold-200);
  font-weight: 600;
}
.orders-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, var(--gold-300), var(--gold-500));
  height: 3px;
  border-radius: 2px;
}
.orders-tabs :deep(.el-tabs__nav-wrap::after) {
  background: var(--border-color);
}

.tab-badge { margin-left: 4px; }

.orders-area {
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.order-card {
  padding: 0;
  overflow: hidden;
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
}
.order-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), 0 20px 50px -24px rgba(212,175,55,0.15);
  border-color: var(--gold-tint-2);
}

/* 头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-xl);
  background:
    linear-gradient(180deg, var(--bg-tertiary), transparent);
  border-bottom: 1px solid var(--border-color);
}
.order-info {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}
.order-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-tertiary);
}
.order-no {
  font-family: var(--font-mono);
  color: var(--text-secondary);
  letter-spacing: 0.02em;
}
.order-no-label {
  color: var(--text-muted);
  margin-right: 6px;
  font-size: var(--fs-xs);
}

/* 商品列表 */
.order-items {
  padding: var(--space-md) var(--space-xl);
}
.order-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) 0;
  border-bottom: 1px dashed var(--border-color);
}
.order-item:last-child { border-bottom: none; }

.item-thumb {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  border: 1px solid var(--border-color);
}
.thumb-err {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12;
  font-size: 22px;
  font-weight: 700;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: var(--fs-base);
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
}
.item-price {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
  margin-top: 4px;
}
.item-subtotal {
  font-size: var(--fs-md);
  color: var(--gold-300);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

/* footer */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) var(--space-xl);
  background: var(--bg-tertiary);
  border-top: 1px solid var(--border-color);
  gap: var(--space-xl);
}
.remark-area {
  flex: 1;
  min-width: 0;
}
.order-remark {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fs-sm);
  color: var(--text-muted);
  padding: 4px 10px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-pill);
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  flex-shrink: 0;
}
.order-total {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.total-label {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}
.total-label b {
  color: var(--text-primary);
  font-weight: 600;
}
.total-amount {
  font-size: 22px;
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  text-shadow: 0 2px 14px var(--gold-glow);
}
.order-actions {
  display: flex;
  gap: var(--space-sm);
}
.btn-pay {
  letter-spacing: 0.06em;
}

.empty-wrap {
  padding: var(--space-2xl) 0;
  display: flex;
  justify-content: center;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  padding: var(--space-xl) 0;
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
}

/* ===== 详情 dialog ===== */
.dialog-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: var(--fs-lg);
  font-weight: 700;
  color: var(--text-emphasis);
}
.dialog-header .dot {
  width: 6px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
}

.section-subtitle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: var(--fs-md);
  font-weight: 600;
  color: var(--text-emphasis);
  margin: var(--space-xl) 0 var(--space-md);
}
.section-subtitle span {
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
  border-radius: 2px;
}

.info-descs :deep(.el-descriptions__label) {
  background: var(--bg-tertiary) !important;
  color: var(--text-secondary) !important;
  border-color: var(--border-color) !important;
}
.info-descs :deep(.el-descriptions__content) {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}

.mini-table :deep(.el-table th) {
  background: var(--bg-tertiary) !important;
  color: var(--text-secondary) !important;
  border-color: var(--border-color) !important;
}
.mini-table :deep(.el-table td) {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}

/* 订单进度条 */
.order-steps {
  margin: var(--space-md) 0 var(--space-xl);
}
.order-steps :deep(.el-step__title) {
  color: var(--text-secondary);
}
.order-steps :deep(.el-step__description) {
  color: var(--text-muted);
  font-size: var(--fs-xs);
}
.order-steps :deep(.el-step.is-process .el-step__title) {
  color: var(--gold-300);
}
.order-steps :deep(.el-step__head.is-process) {
  color: var(--gold-300);
  border-color: var(--gold-300);
}
.order-steps :deep(.el-step__head.is-success) {
  color: var(--gold-300);
  border-color: var(--gold-300);
}

.gold-text {
  color: var(--gold-300);
  font-weight: 700;
}

.detail-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  margin-top: var(--space-lg);
  padding: var(--space-lg) var(--space-xl);
  background: var(--gold-tint);
  border: 1px solid var(--gold-tint-2);
  border-radius: var(--radius);
  gap: 10px;
}
.detail-total span {
  font-size: var(--fs-base);
  color: var(--text-secondary);
  font-weight: 500;
}
.total-amount-big {
  font-size: 26px;
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.02em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 18px var(--gold-glow);
}

@media (max-width: 820px) {
  .order-footer {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-md);
  }
  .footer-right {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-md);
  }
  .order-actions { flex-wrap: wrap; }
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }
  .order-info { flex-wrap: wrap; gap: var(--space-md); }
}
</style>
