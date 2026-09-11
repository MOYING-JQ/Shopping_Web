<template>
  <div class="page-wrap cart-page animate-fade-in-up" v-loading="loading">
    <header class="page-header">
      <div>
        <h1 class="page-title">
          <el-icon :size="22" color="var(--gold-300)"><ShoppingCart /></el-icon>
          我的购物车
        </h1>
        <div class="page-subtitle" v-if="cartList.length > 0">已添加 {{ cartList.length }} 种商品，精心挑选值得拥有</div>
      </div>
    </header>

    <section v-if="cartList.length > 0" class="cart-body">
      <!-- 商品卡片列表 -->
      <div class="cart-list">
        <div
          v-for="(row, idx) in cartList"
          :key="row.id"
          class="cart-item stagger-"
          :class="['stagger-' + Math.min(idx + 1, 8), { disabled: row.stock <= 0 }]"
          :style="{ animationDelay: (idx * 0.04) + 's' }"
        >
          <!-- 选中 -->
          <div class="col-check">
            <el-checkbox
              :model-value="row.selected === 1"
              :disabled="row.stock <= 0"
              @change="(val) => toggleRowSelected(row, val)"
            />
          </div>

          <!-- 图片 -->
          <div class="col-media" @click="goDetail(row.productId)">
            <el-image :src="getProductImage(row)" fit="cover" class="media-img" lazy>
              <template #error>
                <div class="img-err">{{ row.productName?.charAt(0) }}</div>
              </template>
            </el-image>
          </div>

          <!-- 信息 -->
          <div class="col-info" @click="goDetail(row.productId)">
            <h3 class="name">{{ row.productName }}</h3>
            <div class="meta">
              <el-tag v-if="row.stock > 0" size="small" type="success" effect="plain">有货</el-tag>
              <el-tag v-else size="small" type="danger" effect="plain">缺货</el-tag>
              <span class="stock-tip">库存 {{ row.stock }} 件</span>
            </div>
          </div>

          <!-- 单价 -->
          <div class="col-price">
            <span class="unit">{{ formatPrice(row.price) }}</span>
          </div>

          <!-- 数量 -->
          <div class="col-qty">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock"
              size="default"
              controls-position="right"
              :disabled="row.stock <= 0"
              @change="handleQuantityChange(row)"
            />
          </div>

          <!-- 小计 -->
          <div class="col-sub">
            <span class="sub">{{ formatPrice(row.price * row.quantity) }}</span>
          </div>

          <!-- 删除 -->
          <div class="col-act">
            <el-button circle type="danger" plain :icon="Delete" size="small" @click="handleDelete(row)" />
          </div>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="checkout-bar panel">
        <div class="left">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <el-button text type="danger" @click="handleBatchDelete" :disabled="selectedCount === 0">
            删除选中（{{ selectedCount }}）
          </el-button>
        </div>
        <div class="right">
          <div class="summary">
            <div class="row-1">
              已选商品 <b class="num">{{ selectedCount }}</b> 件
            </div>
            <div class="row-2">
              <span class="lbl">应付总额</span>
              <span class="amt">{{ formatPrice(totalAmount) }}</span>
            </div>
          </div>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedCount === 0"
            @click="handleCheckout"
            class="checkout-btn"
          >
            去结算
          </el-button>
        </div>
      </div>
    </section>

    <section v-else class="empty-panel panel">
      <el-empty description="购物车空空如也，快去挑选心仪的商品吧">
        <el-button type="primary" size="large" @click="$router.push('/mall')">
          <el-icon style="margin-right: 6px"><Shop /></el-icon>
          去商城逛逛
        </el-button>
      </el-empty>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart, Shop } from '@element-plus/icons-vue'
import { getCartList, updateCartQuantity, deleteCartItem, batchDeleteCart, selectAllCart, updateCartSelected } from '../api/cart'
import { getProductImage } from '../utils/image'
import bus from '../utils/bus'

const router = useRouter()
const loading = ref(false)
const cartList = ref([])

const selectedItems = computed(() => cartList.value.filter(i => i.selected === 1 && i.stock > 0))
const selectedCount = computed(() => selectedItems.value.length)
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0)
)

const selectAll = ref(false)

function formatPrice(val) {
  return '¥' + Number(val || 0).toFixed(2)
}

// 单行选中/取消 → 同步后端并刷新全选状态
async function toggleRowSelected(row, val) {
  const selected = val ? 1 : 0
  row.selected = selected
  refreshSelectAllState()
  try {
    await updateCartSelected(row.id, selected)
  } catch (e) {
    console.error(e)
    loadCart()
  }
}

function refreshSelectAllState() {
  const available = cartList.value.filter(i => i.stock > 0)
  selectAll.value = available.length > 0 && available.every(i => i.selected === 1)
}

async function handleQuantityChange(row) {
  try {
    await updateCartQuantity(row.id, row.quantity)
    bus.emit('cart:update')
  } catch (e) {
    console.error(e)
    loadCart()
  }
}

async function handleSelectAll(val) {
  try {
    await selectAllCart(val ? 1 : 0)
    cartList.value.forEach(i => {
      if (i.stock > 0) i.selected = val ? 1 : 0
    })
    selectAll.value = val
  } catch (e) {
    console.error(e)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定从购物车删除「${row.productName}」吗？`, '提示', { type: 'warning' })
    await deleteCartItem(row.id)
    bus.emit('cart:update')
    ElMessage.success('已删除')
    loadCart()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleBatchDelete() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', { type: 'warning' })
    const ids = selectedItems.value.map(i => i.id)
    await batchDeleteCart(ids)
    bus.emit('cart:update')
    ElMessage.success('已删除选中商品')
    loadCart()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function handleCheckout() {
  const items = selectedItems.value.map(item => ({
    cartId: item.id,
    productId: item.productId,
    sellerId: item.sellerId || item.userId,
    productName: item.productName,
    price: item.price,
    quantity: item.quantity,
    stock: item.stock
  }))
  sessionStorage.setItem('checkout_items', JSON.stringify(items))
  router.push('/checkout')
}

function goDetail(id) {
  router.push(`/product/${id}`)
}

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data || []
    refreshSelectAllState()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  min-height: 520px;
}

/* 页面主体 */
.cart-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

/* 商品卡片列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.cart-item {
  display: grid;
  grid-template-columns: 40px 96px minmax(180px, 1fr) 120px 170px 130px 48px;
  gap: var(--space-md);
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: var(--transition);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  cursor: default;
}

.cart-item:hover {
  border-color: var(--gold-500);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md), 0 6px 24px -14px var(--gold-glow);
  background: var(--bg-tertiary);
}

.cart-item.disabled {
  opacity: 0.55;
  filter: saturate(0.6);
}

.col-check {
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-media {
  cursor: pointer;
  justify-self: center;
}

.media-img {
  width: 88px;
  height: 88px;
  border-radius: var(--radius);
  display: block;
  border: 1px solid var(--border-color);
  transition: var(--transition-fast);
}

.cart-item:hover .media-img {
  border-color: var(--gold-500);
  transform: scale(1.03);
}

.img-err {
  width: 88px;
  height: 88px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12;
  font-size: 32px;
  font-weight: 700;
}

.col-info {
  cursor: pointer;
  min-width: 0;
  align-self: start;
  padding-top: var(--space-sm);
}

.col-info .name {
  margin: 0 0 var(--space-sm);
  font-size: var(--fs-md);
  font-weight: 600;
  color: var(--text-primary);
  line-height: var(--lh-tight);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: var(--transition-fast);
}

.cart-item:hover .name {
  color: var(--gold-200);
}

.col-info .meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.stock-tip {
  font-size: var(--fs-xs);
  color: var(--text-tertiary);
}

.col-price, .col-sub {
  justify-self: center;
  text-align: center;
}

.col-price .unit {
  color: var(--text-secondary);
  font-size: var(--fs-sm);
  font-variant-numeric: tabular-nums;
}

.col-sub .sub {
  color: var(--gold-300);
  font-weight: 700;
  font-size: var(--fs-lg);
  font-variant-numeric: tabular-nums;
}

.col-qty {
  justify-self: center;
  width: 140px;
}

.col-act {
  justify-self: end;
}

/* 结算栏 */
.checkout-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-lg) var(--space-xl);
  position: sticky;
  bottom: var(--space-lg);
  z-index: 5;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  background: rgba(24, 24, 30, 0.88) !important;
  border: 1px solid var(--gold-tint-2);
  box-shadow: var(--shadow-lg), 0 8px 28px -16px var(--gold-glow);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  animation-delay: 0.2s;
}

.checkout-bar .left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.checkout-bar .right {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.summary .row-1 {
  font-size: var(--fs-sm);
  color: var(--text-secondary);
}
.summary .row-1 .num {
  color: var(--gold-200);
  font-size: var(--fs-md);
  font-weight: 700;
  padding: 0 2px;
}

.summary .row-2 {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.summary .row-2 .lbl {
  font-size: var(--fs-base);
  color: var(--text-secondary);
}
.summary .row-2 .amt {
  font-size: var(--fs-3xl);
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  text-shadow: 0 2px 18px var(--gold-glow);
}

.checkout-btn {
  min-width: 160px;
  height: 52px;
  font-size: var(--fs-md);
  letter-spacing: 0.06em;
}

/* 空状态面板 */
.empty-panel {
  padding: var(--space-3xl) var(--space-xl);
}

/* 响应式适配 */
@media (max-width: 1100px) {
  .cart-item {
    grid-template-columns: 40px 80px 1fr 100px 150px 110px 40px;
    padding: var(--space-sm) var(--space-md);
  }
  .media-img, .img-err { width: 72px; height: 72px; }
}

@media (max-width: 820px) {
  .cart-item {
    grid-template-columns: 40px 72px 1fr;
    grid-template-rows: auto auto auto;
    gap: var(--space-md);
  }
  .col-check { grid-row: span 2; align-self: start; padding-top: var(--space-sm); }
  .col-price { grid-column: 3; grid-row: 2; justify-self: start; text-align: left; }
  .col-qty   { grid-column: 2 / 4; grid-row: 3; justify-self: start; }
  .col-sub   { grid-column: 3; grid-row: 1; justify-self: end; align-self: start; padding-top: var(--space-sm); }
  .col-act   { position: absolute; right: var(--space-md); top: var(--space-md); }
  .cart-item  { position: relative; }

  .checkout-bar {
    flex-direction: column;
    gap: var(--space-md);
    align-items: stretch;
  }
  .checkout-bar .right { justify-content: space-between; }
}
</style>
