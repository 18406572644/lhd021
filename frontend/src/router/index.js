import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'idle-item',
        name: 'IdleItem',
        component: () => import('@/views/idle-item/index.vue'),
        meta: { title: '闲置物品', icon: 'Goods' }
      },
      {
        path: 'idle-item/publish',
        name: 'IdleItemPublish',
        component: () => import('@/views/idle-item/publish.vue'),
        meta: { title: '发布物品', icon: 'Plus', hidden: true }
      },
      {
        path: 'exchange-apply',
        name: 'ExchangeApply',
        component: () => import('@/views/exchange-apply/index.vue'),
        meta: { title: '互换申请', icon: 'RefreshRight' }
      },
      {
        path: 'pickup-point',
        name: 'PickupPoint',
        component: () => import('@/views/pickup-point/index.vue'),
        meta: { title: '自提点管理', icon: 'Location' }
      },
      {
        path: 'claim-record',
        name: 'ClaimRecord',
        component: () => import('@/views/claim-record/index.vue'),
        meta: { title: '领用记录', icon: 'Document' }
      },
      {
        path: 'credit-rating',
        name: 'CreditRating',
        component: () => import('@/views/credit-rating/index.vue'),
        meta: { title: '信用评级', icon: 'Medal' }
      },
      {
        path: 'item-archive',
        name: 'ItemArchive',
        component: () => import('@/views/item-archive/index.vue'),
        meta: { title: '归档管理', icon: 'Folder' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '数据统计', icon: 'DataLine' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 社区闲置物资互助系统` : '社区闲置物资互助系统'
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
