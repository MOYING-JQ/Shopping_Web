<template>
  <div class="page-wrap payment-page animate-fade-in-up">
    <header class="page-header">
      <div>
        <h1 class="page-title">
          <el-icon :size="22" color="var(--gold-300)"><Wallet /></el-icon>
          订单支付
        </h1>
        <div class="page-subtitle">请在 15 分钟内完成支付，超时订单将自动取消</div>
      </div>
    </header>

    <div v-loading="loading" class="payment-content">
      <!-- 订单列表 -->
      <section class="panel orders-panel stagger-1">
        <div class="panel-title">
          <span class="dot" />
          待支付订单
          <em class="order-count">共 {{ orders.length }} 笔</em>
        </div>

        <div v-if="orders.length > 0" class="order-list">
          <div v-for="order in orders" :key="order.id" class="pay-order-item">
            <el-image :src="getOrderProductImage(order)" fit="cover" class="pay-thumb" lazy>
              <template #error>
                <div class="thumb-err">{{ order.productName?.charAt(0) }}</div>
              </template>
            </el-image>
            <div class="pay-info">
              <div class="pay-name">{{ order.productName }}</div>
              <div class="pay-meta">
                <span>单价 {{ formatPrice(order.price) }}</span>
                <span class="sep">×</span>
                <span>{{ order.quantity }} 件</span>
                <span class="sep">·</span>
                <span class="pay-order-no">{{ order.orderNo }}</span>
              </div>
            </div>
            <div class="pay-amount">{{ formatPrice(order.totalAmount) }}</div>
          </div>
        </div>
        <el-empty v-else description="没有待支付的订单" :image-size="120" />
      </section>

      <!-- 支付方式 + 合计 -->
      <section class="panel pay-summary-panel stagger-2">
        <div class="panel-title">
          <span class="dot" />
          支付方式
        </div>

        <div class="pay-methods">
          <label
            v-for="m in payMethods"
            :key="m.id"
            class="pay-method"
            :class="{ active: selectedMethod === m.id }"
          >
            <input type="radio" v-model="selectedMethod" :value="m.id" />
            <el-icon :size="22" :color="m.color"><component :is="m.icon" /></el-icon>
            <div class="method-text">
              <div class="method-name">{{ m.name }}</div>
              <div class="method-desc">{{ m.desc }}</div>
            </div>
          </label>
        </div>

        <div class="pay-total-row">
          <span class="pay-total-label">应付总额</span>
          <span class="pay-total-amount">{{ formatPrice(totalAmount) }}</span>
        </div>

        <div class="pay-actions">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button
            size="large"
            type="primary"
            class="btn-pay"
            :loading="paying"
            :disabled="orders.length === 0"
            @click="handlePay"
          >
            完成支付
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, Money, CreditCard, Phone } from '@element-plus/icons-vue'
import { getOrderById, updateOrderStatus } from '../api/order'
import { getProductImage as getPlaceholderImage } from '../utils/image'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paying = ref(false)
const orders = ref([])
const selectedMethod = ref('balance')

const payMethods = [
  { id: 'balance', name: '余额支付', desc: '使用账户余额支付', icon: Money, color: 'var(--gold-300)' },
  { id: 'alipay', name: '支付宝', desc: '推荐有支付宝账户的用户', icon: CreditCard, color: '#1677ff' },
  { id: 'wechat', name: '微信支付', desc: '使用微信扫码支付', icon: Phone, color: '#07c160' }
]

const totalAmount = computed(() =>
  orders.value.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
)

function formatPrice(val) {
  return '¥' + Number(val || 0).toFixed(2)
}

function getOrderProductImage(order) {
  if (order.productImage && order.productImage.startsWith('http')) {
    return order.productImage
  }
  return getPlaceholderImage({ image: order.productImage, name: order.productName })
}

async function loadOrders() {
  const ids = route.query.orderIds
  if (!ids) {
    ElMessage.warning('未指定订单')
    router.replace('/orders')
    return
  }
  const idList = String(ids).split(',').filter(Boolean).map(Number)
  if (idList.length === 0) {
    ElMessage.warning('订单 ID 无效')
    router.replace('/orders')
    return
  }

  loading.value = true
  try {
    const results = await Promise.allSettled(idList.map(id => getOrderById(id)))
    orders.value = results
      .filter(r => r.status === 'fulfilled')
      .map(r => r.value.data)
      .filter(o => o)
    if (orders.value.length === 0) {
      ElMessage.warning('未找到待支付订单')
      router.replace('/orders')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (orders.value.length === 0) return
  paying.value = true
  try {
    // 每笔订单调用 updateOrderStatus 改为已付款（后端会写入 pay_time）
    const tasks = orders.value.map(o => updateOrderStatus(o.id, 1))
    const results = await Promise.allSettled(tasks)
    const success = results.filter(r => r.status === 'fulfilled').length

    if (success === orders.value.length) {
      ElMessage.success(`支付成功！已支付 ${success} 笔订单`)
      router.replace('/orders')
    } else {
      ElMessage.warning(`支付完成 ${success}/${orders.value.length} 笔，失败的订单请在我的订单中重试`)
      router.replace('/orders')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.payment-page {
  min-height: 520px;
}

.payment-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: var(--space-xl);
  max-width: 1100px;
  margin: 0 auto;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  margin-bottom: var(--space-lg);
}
.panel-title .dot {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
}
.order-count {
  font-size: var(--fs-sm);
  font-weight: 400;
  color: var(--text-muted);
  margin-left: 4px;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}
.pay-order-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  transition: var(--transition);
}
.pay-order-item:hover {
  border-color: var(--gold-tint-2);
  box-shadow: var(--shadow-md);
}
.pay-thumb {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  border: 1px solid var(--border-color);
}
.thumb-err {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  color: #1a1a12;
  font-size: 26px;
  font-weight: 700;
}
.pay-info {
  flex: 1;
  min-width: 0;
}
.pay-name {
  font-size: var(--fs-base);
  font-weight: 600;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pay-meta {
  margin-top: 6px;
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.pay-meta .sep { color: var(--text-muted); }
.pay-order-no {
  font-family: var(--font-mono);
  color: var(--text-muted);
  font-size: var(--fs-xs);
}
.pay-amount {
  font-size: var(--fs-lg);
  font-weight: 800;
  color: var(--gold-300);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

/* 支付方式 */
.pay-methods {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}
.pay-method {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
  background: var(--bg-secondary);
}
.pay-method:hover {
  border-color: var(--gold-tint-2);
}
.pay-method.active {
  border-color: var(--gold-300);
  background: var(--gold-tint);
  box-shadow: 0 0 0 2px var(--gold-tint-2);
}
.pay-method input[type="radio"] {
  accent-color: var(--gold-300);
}
.method-name {
  font-size: var(--fs-base);
  font-weight: 600;
  color: var(--text-primary);
}
.method-desc {
  font-size: var(--fs-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

/* 合计 */
.pay-total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: var(--space-lg) 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--space-lg);
}
.pay-total-label {
  font-size: var(--fs-base);
  color: var(--text-secondary);
}
.pay-total-amount {
  font-size: 30px;
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 18px var(--gold-glow);
}

.pay-actions {
  display: flex;
  gap: var(--space-md);
}
.pay-actions .el-button {
  flex: 1;
}
.btn-pay {
  font-size: var(--fs-base);
  font-weight: 700;
  letter-spacing: 0.08em;
  background: linear-gradient(135deg, var(--gold-300), var(--gold-500));
  border: none;
  color: #1a1a12;
}
.btn-pay:hover {
  background: linear-gradient(135deg, var(--gold-200), var(--gold-400));
}

@media (max-width: 860px) {
  .payment-content {
    grid-template-columns: 1fr;
  }
}
</style>
