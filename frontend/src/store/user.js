import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null
  }),
  actions: {
    async login(loginData) {
      const res = await authApi.login(loginData)
      this.token = res.token
      this.userInfo = res.user
      return res
    },
    async register(registerData) {
      await authApi.register(registerData)
    },
    async getCurrentUser() {
      const res = await authApi.getCurrentUser()
      this.userInfo = res
      return res
    },
    logout() {
      this.token = ''
      this.userInfo = null
    }
  },
  persist: {
    key: 'user-store',
    storage: localStorage
  }
})
