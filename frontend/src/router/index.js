import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { hasToken } from '../utils/auth'
import { setSkipAutoRedirect } from '../api/request'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册', public: true }
  },

  // 买家商城路由
  {
    path: '/',
    component: () => import('../layout/BuyerLayout.vue'),
    redirect: '/mall',
    children: [
      {
        path: 'mall',
        name: 'Mall',
        component: () => import('../views/Mall.vue'),
        meta: { title: '商城首页' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('../views/Checkout.vue'),
        meta: { title: '结算', requiresAuth: true }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('../views/Payment.vue'),
        meta: { title: '订单支付', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('../views/MyOrders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },

  // 卖家管理后台路由
  {
    path: '/admin',
    component: () => import('../layout/SellerLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据看板', requiresAuth: true, requiresSeller: true }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/ProductList.vue'),
        meta: { title: '商品管理', requiresAuth: true, requiresSeller: true }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/OrderList.vue'),
        meta: { title: '订单管理', requiresAuth: true, requiresSeller: true }
      },
      {
        path: 'customers',
        name: 'AdminCustomers',
        component: () => import('../views/CustomerList.vue'),
        meta: { title: '我的客户', requiresAuth: true, requiresSeller: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true, requiresSeller: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title || '商城'} - 电商系统`

  if (to.meta.public) {
    if (hasToken() && (to.path === '/login' || to.path === '/register')) {
      return next('/')
    }
    return next()
  }

  if (to.meta.requiresAuth) {
    if (!hasToken()) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }

    const userStore = useUserStore()
    if (!userStore.userInfo) {
      // 告诉拦截器：路由守卫正在处理 token 刷新，不要自动跳转登录页
      setSkipAutoRedirect(true)
      try {
        // 刷新页面后 accessToken 可能刚好过期，先用它尝试获取用户信息
        await userStore.fetchUserInfo()
      } catch (e) {
        // 第一次失败 → 大概率 accessToken 过期了，用 refreshToken 换新 Token 后再重试一次
        try {
          await userStore.doRefreshToken()
          await userStore.fetchUserInfo()
        } catch (e2) {
          // refresh 也失败 → refreshToken 过期或无效 → 清本地并跳登录
          userStore.clearAuth()
          return next({ path: '/login', query: { redirect: to.fullPath } })
        }
      } finally {
        setSkipAutoRedirect(false)
      }
    }

    if (to.meta.requiresSeller && !userStore.isSeller) {
      return next('/mall')
    }
  }

  next()
})

export default router
