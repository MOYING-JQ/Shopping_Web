<template>
  <div class="page-wrap detail-page animate-fade-in-up" v-loading="loading">
    <div v-if="product" class="panel detail-content">
      <!-- 商品信息区 -->
      <div class="product-section">
        <div class="product-image-box animate-scale-in">
          <el-image
            :src="getProductImage(product)"
            fit="cover"
            class="big-img"
            preview-src-list="[getProductImage(product)]"
            preview-teleported
          >
            <template #placeholder>
              <div class="img-placeholder skeleton-shimmer" />
            </template>
            <template #error>
              <div class="img-error-big">{{ product.name?.charAt(0) }}</div>
            </template>
          </el-image>

          <!-- 状态角标 -->
          <div class="img-badge-row" v-if="product.stock > 0">
            <span class="chip" v-if="product.originalPrice && product.originalPrice > product.price">
              省{{ formatPrice(product.originalPrice - product.price) }}
            </span>
            <span class="chip chip-stock">库存充足</span>
          </div>
        </div>

        <div class="product-meta">
          <h1 class="product-title stagger-1">{{ product.name }}</h1>

          <div class="product-sub stagger-2">
            <el-tag type="primary" effect="plain" size="small">
              {{ product.categoryName || '未分类' }}
            </el-tag>
            <span class="stat"><el-icon :size="12"><TrendCharts /></el-icon> 已售 {{ product.sales }} 件</span>
            <span class="stat" :class="{ 'low-stock': product.stock < 50 }">
              <el-icon :size="12"><Box /></el-icon> 库存 {{ product.stock }}
            </span>
          </div>

          <div class="price-box stagger-3">
            <div class="price-row">
              <span class="price-label">促销价</span>
              <span class="current-price">{{ formatPrice(product.price) }}</span>
              <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                {{ formatPrice(product.originalPrice) }}
              </span>
            </div>
            <div class="price-line" />
          </div>

          <div class="desc-box stagger-4" v-if="product.description">
            <div class="desc-label">商品描述</div>
            <p class="desc-text">{{ product.description }}</p>
          </div>

          <div class="quantity-box stagger-5">
            <span class="quantity-label">购买数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" controls-position="right" />
          </div>

          <div class="action-box stagger-6">
            <el-button class="btn-cart" size="large" :icon="ShoppingCart" @click="handleAddCart" :loading="adding">
              加入购物车
            </el-button>
            <el-button type="primary" class="btn-buy" size="large" :icon="Pointer" @click="handleBuyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- 详情区 -->
      <el-divider class="detail-divider" />
      <div class="detail-info">
        <h3 class="info-title"><span />商品详情</h3>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="商品ID">{{ product.id }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ product.categoryName || '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="现价">{{ formatPrice(product.price) }}</el-descriptions-item>
          <el-descriptions-item label="原价">{{ product.originalPrice ? formatPrice(product.originalPrice) : '—' }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ product.stock }} 件</el-descriptions-item>
          <el-descriptions-item label="累计销量">{{ product.sales }} 件</el-descriptions-item>
        </el-descriptions>

        <div class="desc-full panel-inner" v-if="product.description">
          <h4>商品介绍</h4>
          <p>{{ product.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Pointer, TrendCharts, Box } from '@element-plus/icons-vue'
import { getProductById } from '../api/product'
import { addToCart } from '../api/cart'
import { useUserStore } from '../stores/user'
import { getProductImage } from '../utils/image'
import bus from '../utils/bus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const adding = ref(false)
const product = ref(null)
const quantity = ref(1)

function formatPrice(val) {
  if (!val) return '¥0.00'
  return '¥' + Number(val).toFixed(2)
}

async function loadProduct() {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleAddCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  adding.value = true
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    bus.emit('cart:update')
    ElMessage.success('已加入购物车')
  } catch (e) {
    console.error(e)
  } finally {
    adding.value = false
  }
}

function handleBuyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const buyItem = {
    productId: product.value.id,
    sellerId: product.value.sellerId || product.value.userId,
    productName: product.value.name,
    price: product.value.price,
    quantity: quantity.value,
    stock: product.value.stock
  }
  sessionStorage.setItem('checkout_items', JSON.stringify([buyItem]))
  router.push('/checkout')
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.detail-page {
  min-height: 520px;
}

.detail-content {
  padding: var(--space-2xl);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
}

/* ===== 商品主区 ===== */
.product-section {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: var(--space-2xl);
  align-items: start;
}

.product-image-box {
  position: relative;
  flex-shrink: 0;
}
.big-img {
  width: 440px;
  height: 440px;
  border-radius: var(--radius-lg);
  display: block;
  border: 1px solid var(--border-color);
  transition: var(--transition-slow);
  box-shadow: var(--shadow-md);
}
.product-image-box:hover .big-img {
  transform: translateY(-4px) scale(1.01);
  box-shadow: var(--shadow-lg), 0 20px 40px -20px var(--gold-glow);
  border-color: var(--gold-tint-2);
}

.img-placeholder {
  width: 440px;
  height: 440px;
  border-radius: var(--radius-lg);
}

.img-error-big {
  width: 440px;
  height: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  color: #1a1a12;
  font-size: 112px;
  font-weight: 800;
  border-radius: var(--radius-lg);
}

.img-badge-row {
  position: absolute;
  top: var(--space-md);
  left: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  z-index: 2;
}
.chip-stock {
  background: var(--success-tint);
  color: var(--success);
  border-color: transparent;
}

/* ===== 右侧信息 ===== */
.product-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  padding-top: var(--space-sm);
}

.product-title {
  font-size: var(--fs-2xl);
  color: var(--text-emphasis);
  margin: 0;
  line-height: var(--lh-tight);
  font-weight: 700;
  letter-spacing: -0.01em;
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
}

.product-sub {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
  animation-delay: 0.05s;
}
.product-sub .stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}
.product-sub .low-stock {
  color: var(--danger);
  font-weight: 500;
}

/* ===== 价格区 ===== */
.price-box {
  position: relative;
  padding: var(--space-xl);
  border-radius: var(--radius-lg);
  background:
    linear-gradient(135deg, rgba(212,175,55,0.10), rgba(212,175,55,0.03));
  border: 1px solid var(--gold-tint-2);
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
  animation-delay: 0.1s;
}
.price-box::before {
  content: "限时特惠";
  position: absolute;
  top: 10px;
  right: 14px;
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--gold-200);
  font-weight: 600;
  background: var(--gold-tint);
  padding: 3px 8px;
  border-radius: var(--radius-pill);
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.price-label {
  font-size: var(--fs-sm);
  color: var(--gold-300);
  font-weight: 600;
}
.current-price {
  font-size: 40px;
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.03em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 18px var(--gold-glow);
}
.original-price {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  text-decoration: line-through;
}
.price-line {
  height: 1px;
  background: linear-gradient(90deg, var(--gold-tint-2), transparent);
  margin-top: var(--space-lg);
}

/* ===== 描述 ===== */
.desc-box {
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
  animation-delay: 0.14s;
}
.desc-label {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--space-xs);
  font-weight: 500;
  letter-spacing: 0.02em;
}
.desc-text {
  font-size: var(--fs-base);
  color: var(--text-secondary);
  line-height: var(--lh-loose);
  margin: 0;
}

/* ===== 数量 + 按钮 ===== */
.quantity-box {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
  animation-delay: 0.18s;
}
.quantity-label {
  font-size: var(--fs-sm);
  color: var(--text-secondary);
  font-weight: 500;
}

.action-box {
  display: flex;
  gap: var(--space-md);
  animation: fadeInUp var(--dur-normal) var(--ease-out-quart) both;
  animation-delay: 0.22s;
}
.btn-cart, .btn-buy {
  height: 48px;
  padding: 0 28px;
  font-size: var(--fs-md);
  letter-spacing: 0.04em;
}
.btn-cart {
  background: var(--gold-tint) !important;
  color: var(--gold-200) !important;
  border: 1px solid var(--gold-tint-2) !important;
}
.btn-cart:hover {
  background: var(--gold-tint-2) !important;
  border-color: var(--gold-300) !important;
  transform: translateY(-1px);
}

/* ===== 详情区 ===== */
.detail-divider {
  margin: var(--space-2xl) 0 var(--space-lg);
}

.info-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--fs-lg);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0 0 var(--space-lg);
}
.info-title span {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
}

/* descriptions 定制 */
:deep(.el-descriptions__label) {
  background: var(--bg-tertiary) !important;
  color: var(--text-secondary) !important;
  font-weight: 500;
  width: 120px;
  border-color: var(--border-color) !important;
}
:deep(.el-descriptions__content) {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}

.panel-inner {
  margin-top: var(--space-lg);
  padding: var(--space-lg) var(--space-xl);
  background: var(--bg-tertiary);
  border-radius: var(--radius);
}
.desc-full h4 {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0 0 var(--space-sm);
}
.desc-full p {
  font-size: var(--fs-base);
  color: var(--text-secondary);
  line-height: var(--lh-loose);
  margin: 0;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .product-section {
    grid-template-columns: 360px 1fr;
    gap: var(--space-xl);
  }
  .big-img, .img-placeholder, .img-error-big {
    width: 360px;
    height: 360px;
  }
}
@media (max-width: 820px) {
  .product-section {
    grid-template-columns: 1fr;
  }
  .product-image-box { justify-self: center; }
  .big-img, .img-placeholder, .img-error-big {
    width: 100%;
    max-width: 440px;
    aspect-ratio: 1 / 1;
    height: auto;
  }
  .current-price { font-size: 32px; }
}
</style>
