<template>
  <div class="mall-page animate-fade-in-up">
    <div class="container mall-wrap">
      <!-- 热销排行榜 -->
      <section v-if="hotProducts.length > 0" class="hot-section animate-fade-in-up">
        <div class="hot-header">
          <div class="hot-title-row">
            <span class="hot-flame">🔥</span>
            <h2 class="hot-title">热销榜</h2>
            <span class="hot-sub">Top {{ hotProducts.length }}</span>
          </div>
          <p class="hot-desc">大家都在买，手慢无</p>
        </div>
        <div class="hot-scroll" v-loading="hotLoading">
          <article
            v-for="(product, index) in hotProducts"
            :key="product.id"
            class="hot-card"
            :style="{ animationDelay: (index * 0.05) + 's' }"
            @click="goDetail(product.id)"
          >
            <div class="hot-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <div class="hot-card-media">
              <el-image :src="getProductImage(product)" fit="cover" class="hot-card-img" lazy>
                <template #error>
                  <div class="hot-img-fallback">{{ product.name?.charAt(0) }}</div>
                </template>
              </el-image>
            </div>
            <div class="hot-card-body">
              <h3 class="hot-card-name">{{ product.name }}</h3>
              <div class="hot-card-meta">
                <span class="hot-card-price">{{ formatPrice(product.price) }}</span>
                <span class="hot-card-sales">{{ product.sales }} 人付款</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 顶部工具条：筛选 + 排序 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="result-meta">
            <span class="result-title">{{ activeCategoryName || '全部商品' }}</span>
            <span class="result-count" v-if="total">{{ total.toLocaleString() }} 件商品</span>
            <span class="chip" v-if="searchKeyword">
              搜索：{{ searchKeyword }}
            </span>
          </div>
        </div>

        <div class="toolbar-right">
          <!-- 排序 -->
          <div class="sort-group">
            <span
              class="sort-pill"
              :class="{ active: sortField === 'create_time' }"
              @click="changeSort('create_time')"
            >
              最新
              <el-icon v-if="sortField === 'create_time'">
                <CaretTop v-if="sortOrder === 'desc'" /><CaretBottom v-else />
              </el-icon>
            </span>
            <span
              class="sort-pill"
              :class="{ active: sortField === 'sales' }"
              @click="changeSort('sales')"
            >
              销量
              <el-icon v-if="sortField === 'sales'">
                <CaretTop v-if="sortOrder === 'desc'" /><CaretBottom v-else />
              </el-icon>
            </span>
            <span
              class="sort-pill"
              :class="{ active: sortField === 'price' }"
              @click="changeSort('price')"
            >
              价格
              <el-icon v-if="sortField === 'price'">
                <CaretTop v-if="sortOrder === 'desc'" /><CaretBottom v-else />
              </el-icon>
            </span>
          </div>

          <!-- 价格筛选 -->
          <div class="price-box">
            <el-input-number v-model="minPrice" :min="0" :precision="2" placeholder="最低价" controls-position="right" :step="100" size="default" />
            <span class="price-sep">—</span>
            <el-input-number v-model="maxPrice" :min="0" :precision="2" placeholder="最高价" controls-position="right" :step="100" size="default" />
            <el-button type="primary" size="default" @click="handlePriceFilter">应用</el-button>
          </div>
        </div>
      </div>

      <!-- 商品网格 -->
      <div v-loading="loading" class="list-wrap">
        <div v-if="products.length > 0" class="product-grid">
          <article
            v-for="(product, index) in products"
            :key="product.id"
            class="product-card animate-fade-in-up"
            :style="{ animationDelay: ((index % 10) * 0.04) + 's' }"
            @click="goDetail(product.id)"
          >
            <div class="product-media">
              <el-image :src="getProductImage(product)" fit="cover" class="product-img" lazy>
                <template #placeholder>
                  <div class="img-placeholder skeleton-shimmer"></div>
                </template>
                <template #error>
                  <div class="img-placeholder img-error">
                    <span>{{ product.name?.charAt(0) }}</span>
                  </div>
                </template>
              </el-image>

              <div class="media-badges">
                <span v-if="product.originalPrice && product.originalPrice > product.price" class="badge-discount">
                  -{{ Math.round((1 - product.price / product.originalPrice) * 100) }}%
                </span>
                <span v-if="product.sales && product.sales >= 500" class="badge-hot">
                  热销
                </span>
              </div>

              <div class="media-overlay">
                <el-button type="primary" size="small" class="quick-btn">
                  查看详情
                </el-button>
              </div>
            </div>

            <div class="product-body">
              <div class="product-category-name">{{ product.categoryName || '精选好物' }}</div>
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-price-row">
                <div class="price-block">
                  <span class="price-current">{{ formatPrice(product.price) }}</span>
                  <span v-if="product.originalPrice && product.originalPrice > product.price" class="price-origin">
                    {{ formatPrice(product.originalPrice) }}
                  </span>
                </div>
                <div class="sales-block">
                  <span>{{ product.sales || 0 }}</span>
                  <small>销量</small>
                </div>
              </div>
            </div>
          </article>
        </div>

        <el-empty v-else description="暂无符合条件的商品" />
      </div>

      <!-- 分页 -->
      <div class="pagination-bar" v-if="total > 0">
        <div class="pagination-info">
          第 <b>{{ page.current }}</b> / <b>{{ Math.ceil(total / page.size) || 1 }}</b> 页 ·
          每页 <b>{{ page.size }}</b> 件
        </div>
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="total"
          :page-sizes="[16, 28, 40]"
          layout="prev, pager, next, jumper"
          background
          @size-change="onPageSizeChange"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, inject, watch } from 'vue'
import { useRouter } from 'vue-router'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { getProductPage, getMallHotProducts } from '../api/product'
import { getProductImage } from '../utils/image'
import { getCategoryList } from '../api/category'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const total = ref(0)
const categories = ref([])
const hotProducts = ref([])
const hotLoading = ref(false)

const searchKeyword = inject('searchKeyword')
const activeCategory = inject('activeCategory')

const sortField = ref('create_time')
const sortOrder = ref('desc')
const minPrice = ref(undefined)
const maxPrice = ref(undefined)

const page = reactive({ current: 1, size: 16 })

const activeCategoryName = computed(() => {
  if (!activeCategory.value) return ''
  return categories.value.find(c => c.id === activeCategory.value)?.name || ''
})

function formatPrice(val) {
  if (!val) return '¥0.00'
  return '¥' + Number(val).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function changeSort(field) {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'desc'
  }
  page.current = 1
  loadData()
}

function handlePriceFilter() {
  if (minPrice.value && maxPrice.value && minPrice.value > maxPrice.value) {
    [minPrice.value, maxPrice.value] = [maxPrice.value, minPrice.value]
  }
  page.current = 1
  loadData()
}

function goDetail(id) { router.push(`/product/${id}`) }
function onPageSizeChange() { page.current = 1; loadData() }
function onPageChange() { loadData() }

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      role: 0,
      name: searchKeyword.value || undefined,
      categoryId: activeCategory.value || undefined,
      minPrice: minPrice.value || undefined,
      maxPrice: maxPrice.value || undefined,
      status: 1,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      current: page.current,
      size: page.size
    }
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === null || params[key] === '') delete params[key]
    })
    const res = await getProductPage(params)
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(searchKeyword, () => { page.current = 1; loadData() })
watch(activeCategory, () => { page.current = 1; loadData() })

onMounted(() => {
  loadCategories()
  loadData()
  loadHotProducts()
})

async function loadHotProducts() {
  hotLoading.value = true
  try {
    const res = await getMallHotProducts(10)
    hotProducts.value = res.data || []
  } catch (e) {
    console.error('[热销榜] 加载失败:', e)
  } finally {
    hotLoading.value = false
  }
}
</script>

<style scoped>
.mall-page { min-height: 600px; }
.mall-wrap { padding-top: var(--space-xl); }

/* ===== 热销榜 ===== */
.hot-section {
  margin-bottom: var(--space-2xl);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.hot-header {
  padding: var(--space-lg) var(--space-xl) var(--space-md);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  background:
    radial-gradient(600px 200px at 0% 0%, rgba(212,175,55,0.06), transparent 70%);
  border-bottom: 1px solid var(--border-color);
}
.hot-title-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-sm);
}
.hot-flame { font-size: 22px; line-height: 1; }
.hot-title {
  font-size: var(--fs-xl);
  font-weight: 800;
  color: var(--text-emphasis);
  letter-spacing: -0.02em;
}
.hot-sub {
  font-size: var(--fs-xs);
  font-weight: 700;
  color: var(--gold-300);
  padding: 2px 8px;
  background: var(--gold-tint);
  border-radius: var(--radius-pill);
}
.hot-desc {
  font-size: var(--fs-sm);
  color: var(--text-muted);
}

.hot-scroll {
  display: flex;
  gap: var(--space-md);
  overflow-x: auto;
  padding: var(--space-md) var(--space-xl);
  scrollbar-width: thin;
  scrollbar-color: var(--gold-500) transparent;
}
.hot-scroll::-webkit-scrollbar { height: 6px; }
.hot-scroll::-webkit-scrollbar-thumb { background: var(--gold-500); border-radius: 3px; }
.hot-scroll::-webkit-scrollbar-track { background: transparent; }

.hot-card {
  position: relative;
  flex: 0 0 180px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition:
    transform var(--dur-normal) var(--ease-out-quart),
    border-color var(--transition-fast);
}
.hot-card:hover {
  transform: translateY(-3px);
  border-color: var(--gold-500);
}

.hot-rank {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 800;
  color: #fff;
  background: #5a6a8a;
  border-radius: 0 0 8px 0;
}
.hot-rank.rank-1 {
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  color: #1a1a12;
}
.hot-rank.rank-2 {
  background: linear-gradient(135deg, #d4d8de, #9aa4b2);
  color: #2a2a36;
}
.hot-rank.rank-3 {
  background: linear-gradient(135deg, #d49a6a, #b8702a);
}

.hot-card-media {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--bg-deep);
}
.hot-card-img {
  width: 100%;
  height: 100%;
  transition: transform var(--dur-slow) var(--ease-out-expo);
}
.hot-card:hover .hot-card-img { transform: scale(1.08); }
.hot-img-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 36px;
  font-weight: 700;
  color: var(--gold-500);
  opacity: 0.4;
}

.hot-card-body {
  padding: var(--space-sm) var(--space-sm) var(--space-md);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.hot-card-name {
  font-size: var(--fs-sm);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.6em;
}
.hot-card-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 4px;
}
.hot-card-price {
  font-size: var(--fs-md);
  font-weight: 800;
  color: var(--gold-200);
}
.hot-card-sales {
  font-size: 10px;
  color: var(--text-muted);
  white-space: nowrap;
}

@media (max-width: 780px) {
  .hot-card { flex: 0 0 140px; }
}

/* ===== 工具条 ===== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  flex-wrap: wrap;
  padding: var(--space-base) var(--space-xl);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-xl);
  box-shadow: var(--shadow-soft);
}

.result-meta {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
}
.result-title {
  font-size: var(--fs-lg);
  font-weight: 700;
  color: var(--text-emphasis);
}
.result-count {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  flex-wrap: wrap;
}

/* 排序胶囊组 */
.sort-group {
  display: inline-flex;
  padding: 4px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-pill);
  border: 1px solid var(--border-color);
}
.sort-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  font-size: var(--fs-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition-fast);
  font-weight: 500;
}
.sort-pill .el-icon { font-size: 12px; }
.sort-pill:hover { color: var(--gold-500); }
.sort-pill.active {
  background: linear-gradient(135deg, var(--gold-300), var(--gold));
  color: #1a1a12;
  font-weight: 700;
  box-shadow: 0 4px 12px -4px var(--gold-glow);
}

/* 价格框 */
.price-box {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.price-sep { color: var(--text-muted); font-weight: 600; }

/* ===== 商品网格 ===== */
.list-wrap { min-height: 400px; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}

.product-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition:
    transform var(--dur-normal) var(--ease-out-quart),
    box-shadow var(--dur-normal) var(--ease-out-quart),
    border-color var(--dur-fast) var(--ease-out-quart);
  position: relative;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: var(--gold-500);
  box-shadow: var(--shadow-md), 0 8px 28px -12px var(--gold-glow);
}

/* 媒体区 */
.product-media {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--bg-tertiary);
}
.product-img {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform var(--dur-slow) var(--ease-out-expo);
}
.product-card:hover .product-img { transform: scale(1.06); }

.img-placeholder {
  width: 100%;
  height: 100%;
}
.img-error {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
}
.img-error span {
  font-size: 56px;
  font-weight: 700;
  color: var(--gold-500);
  opacity: 0.5;
}

/* 角标 */
.media-badges {
  position: absolute;
  top: var(--space-sm);
  left: var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: 4px;
  z-index: 2;
}
.badge-discount, .badge-hot {
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.02em;
  backdrop-filter: blur(4px);
}
.badge-discount {
  background: linear-gradient(135deg, var(--danger), #ff7875);
  color: #fff;
  box-shadow: 0 4px 10px -4px rgba(239,68,68,0.5);
}
.badge-hot {
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  color: #1a1a12;
}

/* Hover 覆盖层 */
.media-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.55) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 18px;
  opacity: 0;
  transition: opacity var(--dur-fast) var(--ease-out-quart);
}
.product-card:hover .media-overlay { opacity: 1; }
.quick-btn { width: 70%; }

/* 信息区 */
.product-body {
  padding: var(--space-base) var(--space-md) var(--space-md);
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.product-category-name {
  font-size: var(--fs-xs);
  color: var(--gold-300);
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.product-name {
  font-size: var(--fs-md);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
  transition: color var(--transition-fast);
}
.product-card:hover .product-name { color: var(--gold-200); }

.product-price-row {
  margin-top: auto;
  padding-top: var(--space-sm);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  border-top: 1px dashed var(--border-color);
  padding-top: var(--space-base);
}

.price-block { display: flex; align-items: baseline; gap: 6px; flex-wrap: wrap; }
.price-current {
  font-size: 20px;
  font-weight: 800;
  color: var(--gold-200);
  letter-spacing: -0.01em;
  line-height: 1;
}
.price-origin {
  font-size: var(--fs-xs);
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.sales-block {
  text-align: right;
  display: flex;
  flex-direction: column;
  line-height: 1;
}
.sales-block span {
  font-size: var(--fs-sm);
  color: var(--text-primary);
  font-weight: 700;
}
.sales-block small {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 2px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* ===== 分页 ===== */
.pagination-bar {
  margin-top: var(--space-2xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  flex-wrap: wrap;
  padding: var(--space-base) var(--space-lg);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}
.pagination-info {
  font-size: var(--fs-sm);
  color: var(--text-secondary);
}
.pagination-info b {
  color: var(--gold-200);
  font-weight: 700;
  font-size: var(--fs-md);
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 780px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-base); }
  .price-block { font-size: 16px; }
  .toolbox { flex-direction: column; align-items: stretch; }
  .toolbar-right { width: 100%; justify-content: space-between; }
  .price-box .el-input-number { width: 45%; }
}
</style>
