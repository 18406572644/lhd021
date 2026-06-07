import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { ElMessage } from 'element-plus'

const constantRoutes = [
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
  }
]

const asyncRoutes = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '仪表盘', icon: 'Odometer', permission: 'dashboard:view' }
  },
  {
    path: 'idle-item',
    name: 'IdleItem',
    component: () => import('@/views/idle-item/index.vue'),
    meta: { title: '闲置物品', icon: 'Goods', permission: 'idle-item:view' }
  },
  {
    path: 'idle-item/publish',
    name: 'IdleItemPublish',
    component: () => import('@/views/idle-item/publish.vue'),
    meta: { title: '发布物品', icon: 'Plus', hidden: true, permission: 'idle-item:publish' }
  },
  {
    path: 'exchange-apply',
    name: 'ExchangeApply',
    component: () => import('@/views/exchange-apply/index.vue'),
    meta: { title: '互换申请', icon: 'RefreshRight', permission: 'exchange:view' }
  },
  {
    path: 'pickup-point',
    name: 'PickupPoint',
    component: () => import('@/views/pickup-point/index.vue'),
    meta: { title: '自提点管理', icon: 'Location', permission: 'pickup-point:view', roles: ['SUPER_ADMIN', 'PICKUP_POINT_ADMIN', 'OPERATOR'] }
  },
  {
    path: 'claim-record',
    name: 'ClaimRecord',
    component: () => import('@/views/claim-record/index.vue'),
    meta: { title: '领用记录', icon: 'Document', permission: 'claim:view' }
  },
  {
    path: 'credit-rating',
    name: 'CreditRating',
    component: () => import('@/views/credit-rating/index.vue'),
    meta: { title: '信用评级', icon: 'Medal', permission: 'credit:view' }
  },
  {
    path: 'item-archive',
    name: 'ItemArchive',
    component: () => import('@/views/item-archive/index.vue'),
    meta: { title: '归档管理', icon: 'Folder', permission: 'archive:view' }
  },
  {
    path: 'statistics',
    name: 'Statistics',
    component: () => import('@/views/statistics/index.vue'),
    meta: { title: '数据统计', icon: 'DataLine', permission: 'statistics:view', roles: ['SUPER_ADMIN', 'OPERATOR'] }
  },
  {
    path: 'system',
    name: 'System',
    redirect: '/system/role',
    meta: { title: '系统管理', icon: 'Setting', permission: 'system:view', roles: ['SUPER_ADMIN'] },
    children: [
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system:role:view' }
      },
      {
        path: 'permission',
        name: 'PermissionManagement',
        component: () => import('@/views/system/permission.vue'),
        meta: { title: '权限管理', icon: 'Key', permission: 'system:permission:view' }
      },
      {
        path: 'user-role',
        name: 'UserRoleManagement',
        component: () => import('@/views/system/user-role.vue'),
        meta: { title: '用户角色', icon: 'Avatar', permission: 'system:user:assign' }
      }
    ]
  }
]

const createRoutes = (children) => [
  ...constantRoutes,
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: children
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: createRoutes(asyncRoutes)
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 社区闲置物资互助系统` : '社区闲置物资互助系统'
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  
  if (!to.meta.requiresAuth) {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  if (to.meta.permission && !permissionStore.hasPermission(to.meta.permission)) {
    if (from.path === '/') {
      next('/dashboard')
    } else {
      ElMessage?.error('您没有权限访问该页面')
      next(false)
    }
    return
  }
  
  if (to.meta.roles && !permissionStore.hasAnyRole(to.meta.roles)) {
    if (from.path === '/') {
      next('/dashboard')
    } else {
      ElMessage?.error('您没有权限访问该页面')
      next(false)
    }
    return
  }
  
  next()
})

export default router
