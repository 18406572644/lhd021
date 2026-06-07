import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const pendingRequests = new Map()

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    if (config.confirmToken) {
      config.headers['X-Confirm-Token'] = config.confirmToken
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
        })
      return Promise.reject(new Error(res.message || 'Error'))
    } else if (res.code === 4001) {
      return handleConfirmRequired(response, res.message)
    } else {
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      })
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function handleConfirmRequired(response, message) {
  const config = response.config
  const requestKey = `${config.method}_${config.url}_${JSON.stringify(config.data || config.params)}`

  if (pendingRequests.has(requestKey)) {
    return pendingRequests.get(requestKey)
  }

  const messageParts = message.split(':')
  const token = messageParts.length > 1 ? messageParts[messageParts.length - 1] : ''
  const displayMessage = messageParts[0] || '此操作敏感，需要二次确认'

  const promise = ElMessageBox.confirm(displayMessage, '操作确认', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    type: 'warning',
    distinguishCancelAndClose: true
  }).then(() => {
    const retryConfig = { ...config, confirmToken: token }
    return request(retryConfig)
  }).catch((action) => {
    if (action === 'cancel' || action === 'close') {
      return Promise.reject(new Error('已取消操作'))
    }
    return Promise.reject(new Error('确认失败'))
  }).finally(() => {
    pendingRequests.delete(requestKey)
  })

  pendingRequests.set(requestKey, promise)
  return promise
}

export default request
