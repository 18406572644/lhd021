import { usePermissionStore } from '@/store/permission'

export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const permissionStore = usePermissionStore()
    
    if (value && typeof value === 'string') {
      if (!permissionStore.hasPermission(value)) {
        el.parentNode?.removeChild(el)
      }
    } else if (value && Array.isArray(value)) {
      if (!permissionStore.hasAnyPermission(value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

export const roleDirective = {
  mounted(el, binding) {
    const { value } = binding
    const permissionStore = usePermissionStore()
    
    if (value && typeof value === 'string') {
      if (!permissionStore.hasRole(value)) {
        el.parentNode?.removeChild(el)
      }
    } else if (value && Array.isArray(value)) {
      if (!permissionStore.hasAnyRole(value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}
