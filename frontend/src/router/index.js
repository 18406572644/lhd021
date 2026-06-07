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
    meta: { title: '仪表盘', icon: 'Odometer', permission: 'dashboard' }
  },
  {
    path: 'idle-item',
    name: 'IdleItem',
    component: () => import('@/views/idle-item/index.vue'),
    meta: { title: '闲置物品', icon: 'Goods', permission: 'idle_item' }
  },
  {
    path: 'idle-item/publish',
    name: 'IdleItemPublish',
    component: () => import('@/views/idle-item/publish.vue'),
    meta: { title: '发布物品', icon: 'Plus', hidden: true, permission: 'idle_item_add' }
  },
  {
    path: 'exchange-apply',
    name: 'ExchangeApply',
    component: () => import('@/views/exchange-apply/index.vue'),
    meta: { title: '互换申请', icon: 'RefreshRight', permission: 'exchange_apply' }
  },
  {
    path: 'pickup-point',
    name: 'PickupPoint',
    component: () => import('@/views/pickup-point/index.vue'),
    meta: { title: '自提点管理', icon: 'Location', permission: 'pickup_point', roles: ['SUPER_ADMIN', 'PICKUP_ADMIN', 'OPERATOR'] }
  },
  {
    path: 'claim-record',
    name: 'ClaimRecord',
    component: () => import('@/views/claim-record/index.vue'),
    meta: { title: '领用记录', icon: 'Document', permission: 'claim_record' }
  },
  {
    path: 'credit-rating',
    name: 'CreditRating',
    component: () => import('@/views/credit-rating/index.vue'),
    meta: { title: '信用评级', icon: 'Medal', permission: 'credit_rating' }
  },
  {
    path: 'item-archive',
    name: 'ItemArchive',
    component: () => import('@/views/item-archive/index.vue'),
    meta: { title: '归档管理', icon: 'Folder', permission: 'item_archive' }
  },
  {
    path: 'statistics',
    name: 'Statistics',
    component: () => import('@/views/statistics/index.vue'),
    meta: { title: '数据统计', icon: 'DataLine', permission: 'statistics', roles: ['SUPER_ADMIN', 'OPERATOR'] }
  },
  {
    path: 'system',
    name: 'System',
    redirect: '/system/role',
    meta: { title: '系统管理', icon: 'Setting', permission: 'system', roles: ['SUPER_ADMIN'] },
    children: [
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system_role' }
      },
      {
        path: 'permission',
        name: 'PermissionManagement',
        component: () => import('@/views/system/permission.vue'),
        meta: { title: '权限管理', icon: 'Key', permission: 'system_permission' }
      },
      {
        path: 'user-role',
        name: 'UserRoleManagement',
        component: () => import('@/views/system/user-role.vue'),
        meta: { title: '用户角色', icon: 'Avatar', permission: 'system_user_assign_role' }
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('@/views/system/operation-log/index.vue'),
        meta: { title: '操作日志', icon: 'Document', permission: 'operation_log' }
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

router.beforeEach(async (to, from, next) => {
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

  if (!userStore.userInfo || permissionStore.roles.length === 0) {
    try {
      await userStore.getCurrentUser()
    } catch (e) {
      userStore.logout()
      next('/login')
      return
    }
  }

  const isInitialNavigation = from.path === '/' || from.path === '/login'

  if (to.meta.permission && !permissionStore.hasPermission(to.meta.permission)) {
    if (isInitialNavigation) {
      if (permissionStore.hasPermission('dashboard')) {
        next('/dashboard')
      } else {
        ElMessage?.error('您没有权限访问该页面')
        userStore.logout()
        next('/login')
      }
    } else {
      ElMessage?.error('您没有权限访问该页面')
      next(false)
    }
    return
  }

  if (to.meta.roles && !permissionStore.hasAnyRole(to.meta.roles)) {
    if (isInitialNavigation) {
      if (permissionStore.hasPermission('dashboard')) {
        next('/dashboard')
      } else {
        ElMessage?.error('您没有权限访问该页面')
        userStore.logout()
        next('/login')
      }
    } else {
      ElMessage?.error('您没有权限访问该页面')
      next(false)
    }
    return
  }

  next()
})

export default router
