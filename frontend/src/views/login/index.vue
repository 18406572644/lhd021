<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="login-brand">
          <el-icon :size="48" color="#fff"><Goods /></el-icon>
          <h1>社区闲置物资互助系统</h1>
          <p>物尽其用 · 邻里互助 · 共建和谐社区</p>
        </div>
        <div class="login-features">
          <div class="feature-item">
            <el-icon :size="20"><Select /></el-icon>
            <span>闲置物品发布</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><RefreshRight /></el-icon>
            <span>互换申请审核</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Location /></el-icon>
            <span>自提点管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Medal /></el-icon>
            <span>信用评级体系</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2>欢迎回来</h2>
          <p class="login-subtitle">请登录您的账号</p>
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password>
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
              登录
            </el-button>
          </el-form>
          <div class="login-footer">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
          <div class="login-tips">
            <p>测试账号：admin / 123456（管理员）</p>
            <p>测试账号：zhangsan / 123456（普通用户）</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 1100px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  overflow: hidden;
  min-height: 580px;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  padding: 60px 50px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .login-brand {
    h1 {
      font-size: 28px;
      font-weight: 700;
      margin: 20px 0 10px;
    }
    p {
      color: rgba(255, 255, 255, 0.85);
      font-size: 15px;
    }
  }

  .login-features {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-top: 40px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      font-size: 14px;
    }
  }
}

.login-right {
  width: 480px;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .login-form-wrapper {
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: 8px;
    }
    .login-subtitle {
      color: $text-secondary;
      margin-bottom: 32px;
    }
    .login-btn {
      width: 100%;
      margin-top: 8px;
      height: 46px;
      font-size: 16px;
    }
    .login-footer {
      text-align: center;
      margin-top: 24px;
      color: $text-secondary;
    }
    .login-tips {
      margin-top: 30px;
      padding: 16px;
      background: $bg-primary;
      border-radius: 8px;
      p {
        font-size: 12px;
        color: $text-secondary;
        margin: 4px 0;
      }
    }
  }
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-right {
    width: 100%;
  }
}
</style>
