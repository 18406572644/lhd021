import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './assets/styles/main.scss'
import { permissionDirective, roleDirective } from './directives/permission'
import { hasPermission, hasAnyPermission, hasRole, hasAnyRole } from './utils/permission'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('permission', permissionDirective)
app.directive('role', roleDirective)

app.config.globalProperties.$hasPermission = hasPermission
app.config.globalProperties.$hasAnyPermission = hasAnyPermission
app.config.globalProperties.$hasRole = hasRole
app.config.globalProperties.$hasAnyRole = hasAnyRole

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
