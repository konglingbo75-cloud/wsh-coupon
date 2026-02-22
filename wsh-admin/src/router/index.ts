import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' }
      },
      {
        path: 'merchants',
        name: 'Merchants',
        component: () => import('@/views/Merchants.vue'),
        meta: { title: '商户管理', icon: 'Shop' }
      },
      {
        path: 'merchants/:id',
        name: 'MerchantDetail',
        component: () => import('@/views/MerchantDetail.vue'),
        meta: { title: '商户详情', hidden: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/Activities.vue'),
        meta: { title: '活动管理', icon: 'Present' }
      },
      {
        path: 'billing',
        name: 'Billing',
        component: () => import('@/views/Billing.vue'),
        meta: { title: '费用管理', icon: 'Money' }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/Logs.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth !== false && !authStore.isLoggedIn()) {
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn()) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
