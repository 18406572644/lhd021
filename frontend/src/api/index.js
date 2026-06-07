import request from '@/utils/request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getCurrentUser: () => request.get('/auth/current'),
  logout: () => request.post('/auth/logout'),
  listUsers: () => request.get('/auth/users')
}

export const idleItemApi = {
  publish: (data) => request.post('/idle-item', data),
  update: (id, data) => request.put(`/idle-item/${id}`, data),
  page: (params) => request.get('/idle-item/page', { params }),
  detail: (id) => request.get(`/idle-item/${id}`),
  offline: (id) => request.post(`/idle-item/${id}/offline`),
  batchOffline: (ids) => request.post('/idle-item/offline/batch', ids),
  myPublish: (params) => request.get('/idle-item/my', { params }),
  getMyItems: () => request.get('/idle-item/my', { params: { pageNum: 1, pageSize: 1000 } }),
  getCategories: () => request.get('/idle-item/categories')
}

export const exchangeApplyApi = {
  apply: (data) => request.post('/exchange-apply', data),
  approve: (id, remark) => request.post(`/exchange-apply/${id}/approve`, null, { params: { remark } }),
  reject: (id, rejectReason) => request.post(`/exchange-apply/${id}/reject`, null, { params: { rejectReason } }),
  cancel: (id) => request.post(`/exchange-apply/${id}/cancel`),
  complete: (id) => request.post(`/exchange-apply/${id}/complete`),
  page: (params) => request.get('/exchange-apply/page', { params }),
  myApply: (params) => request.get('/exchange-apply/my-apply', { params }),
  myReceive: (params) => request.get('/exchange-apply/my-receive', { params }),
  detail: (id) => request.get(`/exchange-apply/${id}`),
  getStatusCount: () => request.get('/exchange-apply/status-count')
}

export const pickupPointApi = {
  add: (data) => request.post('/pickup-point', data),
  update: (data) => request.put('/pickup-point', data),
  delete: (id) => request.delete(`/pickup-point/${id}`),
  detail: (id) => request.get(`/pickup-point/${id}`),
  page: (params) => request.get('/pickup-point/page', { params }),
  list: () => request.get('/pickup-point/list'),
  listAll: () => request.get('/pickup-point/list'),
  enable: (id) => request.post(`/pickup-point/${id}/enable`),
  disable: (id) => request.post(`/pickup-point/${id}/disable`)
}

export const claimRecordApi = {
  create: (data) => request.post('/claim-record', data),
  confirm: (id, pickupCode, operatorName) => request.post(`/claim-record/${id}/confirm`, null, { params: { pickupCode, operatorName } }),
  cancel: (id) => request.post(`/claim-record/${id}/cancel`),
  page: (params) => request.get('/claim-record/page', { params }),
  myClaim: (params) => request.get('/claim-record/my', { params }),
  detail: (id) => request.get(`/claim-record/${id}`)
}

export const creditRatingApi = {
  page: (params) => request.get('/credit-rating/page', { params }),
  myRecord: (params) => request.get('/credit-rating/my', { params }),
  getUserHistory: (userId) => request.get(`/credit-rating/user/${userId}`),
  getLevelConfig: () => request.get('/credit-rating/level-config'),
  calculateLevel: (userId) => request.post(`/credit-rating/calculate/${userId}`),
  adjust: (data) => request.post('/credit-rating/adjust', data)
}

export const itemArchiveApi = {
  archive: (itemId, reason) => request.post(`/item-archive/archive/${itemId}`, null, { params: { reason } }),
  batchArchive: (itemIds, reason) => request.post('/item-archive/archive/batch', itemIds, { params: { reason } }),
  autoArchive: () => request.post('/item-archive/auto-archive'),
  page: (params) => request.get('/item-archive/page', { params }),
  detail: (id) => request.get(`/item-archive/${id}`),
  restore: (id) => request.post(`/item-archive/restore/${id}`),
  getPendingArchive: (params) => request.get('/idle-item/page', { params: { ...params, status: '已下架' } })
}

export const statisticsApi = {
  generateMonthly: (month) => request.post(`/statistics/generate/${month}`),
  generateCurrent: () => request.post('/statistics/generate/current'),
  generate: (month) => request.post(`/statistics/generate/${month}`),
  page: (params) => request.get('/statistics/page', { params }),
  detail: (id) => request.get(`/statistics/${id}`),
  getByMonth: (month) => request.get(`/statistics/month/${month}`),
  getMonthly: (month) => request.get(`/statistics/month/${month}`),
  getDashboard: () => request.get('/statistics/dashboard'),
  getOverview: () => request.get('/statistics/dashboard'),
  getTrendData: (months) => request.get('/statistics/trend', { params: { months } }),
  getTrend: () => request.get('/statistics/trend'),
  getCategoryStats: () => request.get('/statistics/category-stats'),
  getCreditDistribution: () => request.get('/statistics/credit-distribution'),
  getHistory: () => request.get('/statistics/page', { params: { pageNum: 1, pageSize: 12 } })
}

export const systemApi = {
  getMenuTree: () => request.get('/system/menu/tree'),
  getPermissionTree: () => request.get('/system/permission/tree'),
  getPermissionList: () => request.get('/system/permission/list'),
  addPermission: (data) => request.post('/system/permission', data),
  updatePermission: (data) => request.put('/system/permission', data),
  deletePermission: (id) => request.delete(`/system/permission/${id}`),
  
  getRolePage: (params) => request.get('/system/role/page', { params }),
  getRoleList: () => request.get('/system/role/list'),
  getRoleDetail: (id) => request.get(`/system/role/${id}`),
  addRole: (data) => request.post('/system/role', data),
  updateRole: (data) => request.put('/system/role', data),
  deleteRole: (id) => request.delete(`/system/role/${id}`),
  assignPermissions: (roleId, permissionIds) => request.post(`/system/role/${roleId}/permissions`, permissionIds),
  assignDataPermissions: (roleId, data) => request.post(`/system/role/${roleId}/data-permissions`, data),
  getUserRoles: (userId) => request.get(`/system/user/${userId}/roles`),
  assignUserRoles: (userId, roleIds) => request.post(`/system/user/${userId}/roles`, roleIds)
}

export const api = {
  auth: authApi,
  idleItem: idleItemApi,
  exchangeApply: exchangeApplyApi,
  pickupPoint: pickupPointApi,
  claimRecord: claimRecordApi,
  creditRating: creditRatingApi,
  itemArchive: itemArchiveApi,
  statistics: statisticsApi,
  system: systemApi
}
