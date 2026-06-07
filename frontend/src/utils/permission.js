import { usePermissionStore } from '@/store/permission'

export function hasPermission(permission) {
  const permissionStore = usePermissionStore()
  return permissionStore.hasPermission(permission)
}

export function hasAnyPermission(permissions) {
  const permissionStore = usePermissionStore()
  return permissionStore.hasAnyPermission(permissions)
}

export function hasRole(role) {
  const permissionStore = usePermissionStore()
  return permissionStore.hasRole(role)
}

export function hasAnyRole(roles) {
  const permissionStore = usePermissionStore()
  return permissionStore.hasAnyRole(roles)
}

export function buildMenuRoutes(menuTree, components) {
  const routes = []
  
  function buildRoute(menu, parentPath = '') {
    const path = parentPath ? `${parentPath}/${menu.path}` : menu.path
    const route = {
      path: path,
      name: menu.name,
      meta: {
        title: menu.name,
        icon: menu.icon,
        permission: menu.permission,
        hidden: menu.visible === 0
      }
    }
    
    if (menu.component && components[menu.component]) {
      route.component = components[menu.component]
    }
    
    if (menu.children && menu.children.length > 0) {
      route.children = menu.children.map(child => buildRoute(child, path))
    }
    
    return route
  }
  
  menuTree.forEach(menu => {
    routes.push(buildRoute(menu))
  })
  
  return routes
}

export function filterRoutesByPermission(routes, permissionStore) {
  return routes.filter(route => {
    if (route.meta?.permission) {
      if (!permissionStore.hasPermission(route.meta.permission)) {
        return false
      }
    }
    if (route.meta?.roles) {
      if (!permissionStore.hasAnyRole(route.meta.roles)) {
        return false
      }
    }
    if (route.children) {
      route.children = filterRoutesByPermission(route.children, permissionStore)
    }
    return true
  })
}
