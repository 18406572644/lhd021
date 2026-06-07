<template>
  <div class="layout-container">
    <el-container class="layout-wrapper">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
        <div class="logo">
          <el-icon :size="28" color="#fff"><Goods /></el-icon>
          <span v-show="!isCollapse" class="logo-text">互助系统</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          background-color="#1e88e5"
          text-color="#fff"
          active-text-color="#fff"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <template v-if="route.children && route.children.length > 0">
              <el-sub-menu :index="'/' + route.path">
                <template #title>
                  <el-icon><component :is="route.meta.icon" /></el-icon>
                  <span>{{ route.meta.title }}</span>
                </template>
                <el-menu-item 
                  v-for="child in route.children" 
                  :key="child.path" 
                  :index="'/' + route.path + '/' + child.path"
                >
                  <el-icon><component :is="child.meta.icon" /></el-icon>
                  <template #title>{{ child.meta.title }}</template>
                </el-menu-item>
              </el-sub-menu>
            </template>
            <template v-else>
              <el-menu-item :index="'/' + route.path">
                <el-icon><component :is="route.meta.icon" /></el-icon>
                <template #title>{{ route.meta.title }}</template>
              </el-menu-item>
            </template>
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="layout-header">
          <div class="header-left">
            <el-icon class="collapse-btn" :size="20" @click="toggleCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <span class="credit-tag" :class="getCreditClass(userStore.userInfo?.creditLevel)">
                  {{ userStore.userInfo?.creditLevel }}
                </span>
                <el-icon><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="credit">
                    <el-icon><Medal /></el-icon>信用记录
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="layout-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)

const permissionStore = usePermissionStore()

const menuRoutes = computed(() => {
  const rootRoute = router.options.routes.find(r => r.path === '/')
  if (!rootRoute || !rootRoute.children) return []
  
  function filterRoutes(routes) {
    return routes.filter(route => {
      if (route.meta?.hidden) return false
      
      if (route.meta?.permission && !permissionStore.hasPermission(route.meta.permission)) {
        return false
      }
      if (route.meta?.roles && !permissionStore.hasAnyRole(route.meta.roles)) {
        return false
      }
      if (route.children) {
        route.children = filterRoutes(route.children)
        return route.children.length > 0
      }
      return true
    })
  }
  
  return filterRoutes([...rootRoute.children])
})

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  return route.matched.filter(r => r.meta?.title)
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const getCreditClass = (level) => {
  const map = {
    '优秀': 'credit-excellent',
    '良好': 'credit-good',
    '一般': 'credit-average',
    '较差': 'credit-poor',
    '很差': 'credit-very-poor'
  }
  return map[level] || 'credit-good'
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/credit-rating')
      break
    case 'credit':
      router.push('/credit-rating')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.layout-wrapper {
  height: 100%;
}

.layout-aside {
  background: $primary-color;
  transition: width $transition-normal;
  overflow: hidden;

  .logo {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }

  :deep(.el-menu) {
    border-right: none;
    .el-menu-item {
      &:hover {
        background: $primary-dark !important;
      }
      &.is-active {
        background: $primary-dark !important;
      }
    }
  }
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid $border-light;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: $header-height;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    .collapse-btn {
      cursor: pointer;
      color: $text-secondary;
      transition: color $transition-fast;
      &:hover {
        color: $primary-color;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 6px;
      transition: background $transition-fast;
      &:hover {
        background: $bg-primary;
      }
      .username {
        font-weight: 500;
        color: $text-primary;
      }
      .credit-tag {
        padding: 2px 8px;
        border-radius: 10px;
        font-size: 12px;
        background: $bg-primary;
      }
    }
  }
}

.layout-main {
  padding: 0;
  background: $bg-primary;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
