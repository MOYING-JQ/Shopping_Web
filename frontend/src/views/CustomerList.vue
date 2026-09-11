<template>
  <div class="admin-page animate-fade-in-up customer-list-page">
    <header class="admin-header stagger-1">
      <div>
        <h1 class="admin-title">
          <el-icon :size="22" color="var(--gold-300)"><Avatar /></el-icon>
          我的客户
        </h1>
        <div class="admin-subtitle">购买过您商品的客户列表</div>
      </div>
    </header>

    <section class="panel admin-filter stagger-2">
      <el-form :inline="true" :model="query" class="filter-form">
        <div class="filter-row">
          <div class="filter-field">
            <label>客户名称</label>
            <el-input v-model="query.username" placeholder="搜索用户名" clearable prefix-icon="User" />
          </div>
          <div class="filter-field">
            <label>手机号码</label>
            <el-input v-model="query.phone" placeholder="搜索手机号" clearable prefix-icon="Phone" />
          </div>
          <div class="filter-field narrow">
            <label>会员等级</label>
            <el-select v-model="query.level" placeholder="全部" clearable class="full-w">
              <el-option label="普通会员" :value="1" />
              <el-option label="银卡会员" :value="2" />
              <el-option label="金卡会员" :value="3" />
            </el-select>
          </div>
          <div class="filter-field narrow">
            <label>账户状态</label>
            <el-select v-model="query.status" placeholder="全部" clearable class="full-w">
              <el-option label="启用中" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
          </div>
          <div class="filter-actions-main">
            <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
            <el-button icon="RefreshLeft" @click="handleReset">重置</el-button>
          </div>
        </div>
        <el-collapse-transition>
          <div v-show="showAdvanced" class="filter-row filter-adv">
            <div class="filter-field narrow">
              <label>性别</label>
              <el-select v-model="query.gender" placeholder="全部" clearable class="full-w">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
                <el-option label="未知" :value="0" />
              </el-select>
            </div>
            <div class="filter-field filter-wide">
              <label>注册时间</label>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="—"
                start-placeholder="起始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                :default-time="[new Date(0,0,0,0,0,0), new Date(0,0,0,23,59,59)]"
                class="full-w"
              />
            </div>
          </div>
        </el-collapse-transition>
        <div class="filter-footer d-flex-between">
          <div class="filter-stat" v-if="activeFilterCount > 0">
            <el-icon color="var(--gold-300)"><Filter /></el-icon>
            已启用 <b>{{ activeFilterCount }}</b> 个筛选条件
          </div>
          <el-button link type="primary" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? '收起高级筛选' : '高级筛选' }}
            <el-icon style="margin-left: 2px" :size="12"><ArrowUp v-if="showAdvanced" /><ArrowDown v-else /></el-icon>
          </el-button>
        </div>
      </el-form>
    </section>

    <section class="panel admin-table-wrap stagger-3">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <span class="toolbar-hint">共 {{ total }} 位客户</span>
        </div>
        <div class="toolbar-right">
          <div class="level-legend">
            <div class="level-chip chip-1"><em />普通</div>
            <div class="level-chip chip-2"><em />银卡</div>
            <div class="level-chip chip-3"><em />金卡</div>
          </div>
        </div>
      </div>

      <div class="el-table-wrap">
        <el-table :data="tableData" stripe v-loading="loading" style="width: 100%" class="admin-table">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="客户信息" min-width="200">
            <template #default="{ row }">
              <div class="cell-customer">
                <div class="avatar" :class="'lv-' + (row.level || 1)">
                  {{ (row.username || 'U').charAt(0).toUpperCase() }}
                </div>
                <div class="user-meta">
                  <div class="user-name-row">
                    <span class="user-name">{{ row.username }}</span>
                    <el-tag v-if="row.level === 3" type="warning" effect="light" size="small" round>VIP</el-tag>
                  </div>
                  <div class="user-sub" v-if="row.email">{{ row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="140" align="center">
            <template #default="{ row }">
              <span v-if="row.phone" class="mono">{{ row.phone }}</span>
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="plain" round
                :type="row.gender === 1 ? 'primary' : (row.gender === 2 ? 'danger' : 'info')">
                {{ genderMap[row.gender] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="会员等级" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="levelTagType(row.level)" effect="dark" round size="small">
                {{ levelMap[row.level] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="订单数" width="100" align="center">
            <template #default="{ row }">
              <span class="order-count">{{ row.orderCount ?? 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="累计消费" width="130" align="center">
            <template #default="{ row }">
              <span class="total-amount">{{ formatMoney(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最近下单" width="170" align="center">
            <template #default="{ row }">
              <span v-if="row.lastOrderTime" class="mono">{{ row.lastOrderTime }}</span>
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="账户状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="registerTime" label="注册时间" width="170" align="center" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Avatar, User, Phone, Search, RefreshLeft, Filter, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { getCustomerPage } from '../api/customer'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)
const showAdvanced = ref(false)

const genderMap = { 0: '未知', 1: '男', 2: '女' }
const levelMap = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员' }

const query = reactive({
  username: '',
  phone: '',
  gender: undefined,
  level: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined,
  current: 1,
  size: 10
})

const activeFilterCount = computed(() => {
  const keys = ['username','phone','gender','level','status','startTime','endTime']
  return keys.reduce((n,k) => (query[k] !== '' && query[k] !== null && query[k] !== undefined ? n + 1 : n), 0)
})

function levelTagType(lv) {
  const m = { 1: 'info', 2: '', 3: 'warning' }
  return m[lv] || 'info'
}

function formatMoney(val) {
  return '¥' + Number(val || 0).toFixed(2)
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
    const params = {}
    Object.keys(query).forEach(k => {
      if (query[k] !== '' && query[k] !== null && query[k] !== undefined) params[k] = query[k]
    })
    params.sellerId = userStore.userInfo?.id
    const res = await getCustomerPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载客户列表失败', e)
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.current = 1; loadData() }
function handleReset() {
  dateRange.value = null
  Object.assign(query, {
    username:'', phone:'', gender:undefined, level:undefined,
    status:undefined, startTime:undefined, endTime:undefined,
    current:1, size:10
  })
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.customer-list-page { display: flex; flex-direction: column; gap: var(--space-xl); }

.filter-form { display: flex; flex-direction: column; gap: var(--space-lg); }
.filter-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: var(--space-md);
  align-items: end;
}
.filter-adv { grid-template-columns: 1fr 2fr; }
.filter-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  min-width: 0;
}
.filter-field label {
  font-size: var(--fs-xs); color: var(--text-muted);
  letter-spacing: 0.04em; padding-left: 2px;
}
.filter-field.narrow { min-width: 120px; }
.filter-field.filter-wide { grid-column: span 1; }

.filter-actions-main {
  display: flex; align-items: center; gap: var(--space-sm); align-self: end; flex-wrap: wrap;
}
.filter-footer {
  display: flex; align-items: center; justify-content: space-between;
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
.toolbar-hint {
  font-size: var(--fs-sm); color: var(--text-secondary);
}

.level-legend { display: inline-flex; gap: var(--space-sm); align-items: center; }
.level-chip {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: var(--fs-xs); color: var(--text-secondary);
  padding: 4px 10px; background: var(--bg-tertiary);
  border: 1px solid var(--border-color); border-radius: var(--radius-pill);
}
.level-chip em {
  width: 8px; height: 8px; border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.05);
}
.chip-1 em { background: linear-gradient(135deg, #8fa0b8, #5a6a8a); }
.chip-2 em { background: linear-gradient(135deg, #d0d4da, #9aa4b2); }
.chip-3 em { background: linear-gradient(135deg, var(--gold-200), var(--gold-500)); }

/* table */
.el-table-wrap {
  border-radius: var(--radius-md); overflow: hidden;
  border: 1px solid var(--border-color);
}
.cell-customer {
  display: flex; align-items: center; gap: var(--space-sm);
}
.avatar {
  width: 42px; height: 42px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 14px;
  color: #fff;
  flex-shrink: 0;
  position: relative;
  box-shadow: 0 4px 12px -4px rgba(0,0,0,0.4);
}
.avatar.lv-1 { background: linear-gradient(135deg, #8fa0b8, #5a6a8a); }
.avatar.lv-2 { background: linear-gradient(135deg, #e0e4ea, #9aa4b2); color: #363642; }
.avatar.lv-3 {
  background: linear-gradient(135deg, var(--gold-200), var(--gold-500));
  color: #1a1a12;
  box-shadow: 0 4px 16px -2px var(--gold-glow);
}
.avatar::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.15);
  pointer-events: none;
}

.user-meta { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.user-name-row {
  display: flex; align-items: center; gap: var(--space-xs);
}
.user-name {
  font-size: var(--fs-base); font-weight: 600; color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: 150px;
}
.user-sub {
  font-size: var(--fs-xs); color: var(--text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: 160px;
}
.mono { font-family: var(--font-mono); font-size: var(--fs-sm); color: var(--text-primary); }
.cell-muted { color: var(--text-muted); }

.order-count {
  font-weight: 700; color: var(--gold-300);
  font-size: var(--fs-base);
}
.total-amount {
  font-weight: 600; color: var(--text-primary);
  font-family: var(--font-mono); font-size: var(--fs-sm);
}

.pagination-bar { display: flex; justify-content: flex-end; padding: var(--space-lg) 0 0; }

@media (max-width: 1200px) {
  .filter-row { grid-template-columns: repeat(2, minmax(0,1fr)) auto; }
  .filter-adv { grid-template-columns: 1fr; }
  .table-toolbar { flex-wrap: wrap; }
}
@media (max-width: 720px) {
  .filter-row, .filter-adv { grid-template-columns: 1fr; }
}
</style>
