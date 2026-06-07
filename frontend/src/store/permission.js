import { defineStore } from 'pinia'
import { systemApi } from '@/api'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [],
    roles: [],
    menuTree: [],
    apiPermissions: [],
    dataPermissions: [],
    dataScopeType: 1
  }),
  getters: {
    hasPermission: (state) => (permission) => {
      if (state.roles.includes('SUPER_ADMIN')) return true
      return state.permissions.includes(permission) || state.apiPermissions.includes(permission)
    },
    hasAnyPermission: (state) => (permissions) => {
      if (state.roles.includes('SUPER_ADMIN')) return true
      return permissions.some(p => state.permissions.includes(p) || state.apiPermissions.includes(p))
    },
    hasRole: (state) => (role) => {
      if (state.roles.includes('SUPER_ADMIN')) return true
      return state.roles.includes(role)
    },
    hasAnyRole: (state) => (roles) => {
      if (state.roles.includes('SUPER_ADMIN')) return true
      return roles.some(r => state.roles.includes(r))
    }
  },
  actions: {
    async loadMenuTree() {
      try {
        const res = await systemApi.getMenuTree()
        this.menuTree = res || []
        return this.menuTree
      } catch (error) {
        console.error('加载菜单树失败:', error)
        return []
      }
    },
    setPermissions(userInfo) {
      if (userInfo) {
        this.permissions = userInfo.permissions || []
        this.roles = userInfo.roles || []
        this.apiPermissions = userInfo.apiPermissions || []
        this.dataPermissions = userInfo.dataPermissions || []
        this.dataScopeType = userInfo.dataScopeType || 1
      }
    },
    clearPermissions() {
      this.permissions = []
      this.roles = []
      this.menuTree = []
      this.apiPermissions = []
      this.dataPermissions = []
      this.dataScopeType = 1
    }
  },
  persist: {
    key: 'permission-store',
    storage: localStorage
  }
})
