<template>
  <div class="page-wrap checkout-page animate-fade-in-up" v-loading="loading">
    <header class="page-header">
      <div>
        <h1 class="page-title">
          <el-icon :size="22" color="var(--gold-300)"><CreditCard /></el-icon>
          确认订单
        </h1>
        <div class="page-subtitle">请核对您的收货信息与商品清单，确认无误后提交订单</div>
      </div>
    </header>

    <div class="checkout-grid">
      <!-- 左侧：表单 -->
      <div class="left-col">
        <!-- 收货地址 -->
        <section class="panel section stagger-1">
          <h3 class="section-title"><span />收货地址</h3>
          <el-form :model="address" label-width="86px" class="addr-form">
            <el-form-item label="收货人" required>
              <el-input v-model="address.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input v-model="address.receiverPhone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="详细地址" required>
              <el-input v-model="address.address" placeholder="请输入详细收货地址（省市区街道门牌号）" />
            </el-form-item>
          </el-form>
        </section>

        <!-- 商品清单 -->
        <section class="panel section stagger-2">
          <h3 class="section-title"><span />商品清单
            <span class="count-chip">共 {{ totalCount }} 件</span>
          </h3>
          <ul class="items-list">
            <li v-for="row in checkoutItems" :key="row.productId" class="item-row">
              <el-image :src="getProductImage(row)" fit="cover" class="thumb" lazy>
                <template #error>
                  <div class="thumb-err">{{ row.productName?.charAt(0) }}</div>
                </template>
              </el-image>
              <div class="item-meta">
                <div class="item-name">{{ row.productName }}</div>
                <div class="item-price">{{ formatPrice(row.price) }} × {{ row.quantity }}</div>
              </div>
              <div class="item-subtotal">{{ formatPrice(row.price * row.quantity) }}</div>
            </li>
          </ul>
        </section>

        <!-- 备注 -->
        <section class="panel section stagger-3">
          <h3 class="section-title"><span />订单备注 <span class="optional">（选填）</span></h3>
          <el-input v-model="remark" placeholder="给商家留言，例如：颜色/尺码/送货时间等" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </section>
      </div>

      <!-- 右侧：金额汇总 + 提交按钮 -->
      <aside class="right-col">
        <div class="summary-panel panel stagger-2">
          <h3 class="summary-title">费用明细</h3>
          <ul class="summary-list">
            <li><span>商品件数</span><b>{{ totalCount }} 件</b></li>
            <li><span>商品总额</span><b>{{ formatPrice(totalAmount) }}</b></li>
            <li><span>运费</span><b class="free">免运费</b></li>
          </ul>
          <div class="summary-total">
            <span>应付总额</span>
            <b class="total-amount">{{ formatPrice(totalAmount) }}</b>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交订单
          </el-button>
          <p class="notice">
            <el-icon :size="12" color="var(--warning)"><Warning /></el-icon>
            提交即表示您同意《商品购买协议》
          </p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CreditCard, Warning } from '@element-plus/icons-vue'
import { addOrder } from '../api/order'
import { useUserStore } from '../stores/user'
import { getProductImage } from '../utils/image'
import bus from '../utils/bus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const checkoutItems = ref([])
const remark = ref('')
const address = ref({
  receiverName: '',
  receiverPhone: '',
  address: ''
})

const totalCount = computed(() => checkoutItems.value.reduce((sum, i) => sum + i.quantity, 0))
const totalAmount = computed(() => checkoutItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0))

function formatPrice(val) {
  return '¥' + Number(val || 0).toFixed(2)
}

async function handleSubmit() {
  if (!address.value.receiverName || !address.value.receiverPhone || !address.value.address) {
    ElMessage.warning('请填写完整的收货地址')
    return
  }

  submitting.value = true
  try {
    // 每个商品单独创建一笔订单，全部信息直接存订单表（不再使用 t_order_item）
    const orderPromises = checkoutItems.value.map(item => {
      return addOrder({
        customerId: userStore.userInfo.id,
        sellerId: item.sellerId,
        productId: item.productId,
        productName: item.productName,
        price: item.price,
        quantity: item.quantity,
        remark: remark.value,
        receiverName: address.value.receiverName,
        receiverPhone: address.value.receiverPhone,
        receiverAddress: address.value.address
      })
    })

    const results = await Promise.allSettled(orderPromises)
    const successIds = results
      .filter(r => r.status === 'fulfilled')
      .map(r => r.value?.data)
      .filter(id => id != null)
    const failed = results.filter(r => r.status === 'rejected')

    if (failed.length > 0) {
      console.error('部分订单创建失败:', failed)
      ElMessage.warning(`共 ${results.length} 笔订单，${failed.length} 笔创建失败`)
    }

    // 清理已结算的购物车商品
    const cartIds = checkoutItems.value.filter(i => i.cartId).map(i => i.cartId)
    if (cartIds.length > 0) {
      try {
        const { batchDeleteCart } = await import('../api/cart')
        await batchDeleteCart(cartIds)
      } catch (e) {
        console.error('清理购物车失败', e)
      }
    }
    sessionStorage.removeItem('checkout_items')
    bus.emit('cart:update')

    // 有成功创建的订单 → 跳转到支付页；否则回订单列表
    if (successIds.length > 0) {
      router.push({ path: '/payment', query: { orderIds: successIds.join(',') } })
    } else {
      ElMessage.error('订单创建失败，请稍后重试')
      router.push('/orders')
    }
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const items = sessionStorage.getItem('checkout_items')
  if (items) {
    checkoutItems.value = JSON.parse(items)
  } else {
    ElMessage.warning('没有可结算的商品')
    router.push('/cart')
  }
})
</script>

<style scoped>
.checkout-page {
  min-height: 520px;
}

.checkout-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--space-xl);
  align-items: start;
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.section {
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  padding: var(--space-xl);
}

.section-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0 0 var(--space-lg);
}
.section-title span {
  width: 3px;
  height: 16px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
  border-radius: 2px;
}
.section-title .count-chip {
  margin-left: var(--space-xs);
  font-size: var(--fs-xs);
  color: var(--gold-200);
  background: var(--gold-tint);
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-weight: 500;
}
.section-title .optional {
  font-size: var(--fs-xs);
  color: var(--text-muted);
  font-weight: 400;
  margin-left: 4px;
}

.addr-form {
  max-width: 560px;
}

/* === 商品列表 === */
.items-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
}
.item-row {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) 0;
  border-bottom: 1px dashed var(--border-color);
}
.item-row:last-child { border-bottom: none; }

.thumb {
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
  font-weight: 700;
  font-size: 24px;
}

.item-meta {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: var(--fs-base);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-price {
  font-size: var(--fs-sm);
  color: var(--text-tertiary);
}
.item-subtotal {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--gold-300);
  font-variant-numeric: tabular-nums;
}

/* === 右侧汇总 === */
.right-col {
  position: sticky;
  top: var(--space-lg);
}

.summary-panel {
  padding: var(--space-xl);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  animation-delay: 0.1s;
}

.summary-title {
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0 0 var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--border-color);
}

.summary-list {
  list-style: none;
  padding: 0;
  margin: 0 0 var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}
.summary-list li {
  display: flex;
  justify-content: space-between;
  font-size: var(--fs-sm);
  color: var(--text-secondary);
}
.summary-list li b {
  color: var(--text-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.summary-list li b.free {
  color: var(--success);
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: var(--space-lg);
  border-radius: var(--radius);
  background: var(--gold-tint);
  border: 1px solid var(--gold-tint-2);
  margin-bottom: var(--space-lg);
}
.summary-total span {
  font-size: var(--fs-base);
  color: var(--text-secondary);
  font-weight: 500;
}
.total-amount {
  font-size: 28px;
  font-weight: 800;
  color: var(--gold-300);
  letter-spacing: -0.02em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 2px 18px var(--gold-glow);
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: var(--fs-md);
  letter-spacing: 0.08em;
}

.notice {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fs-xs);
  color: var(--text-muted);
  margin: var(--space-sm) 0 0;
  text-align: center;
  justify-content: center;
}

@media (max-width: 1000px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }
  .right-col { position: static; }
}
</style>
