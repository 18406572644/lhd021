<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-header">
        <el-icon :size="40" color="#1e88e5"><Goods /></el-icon>
        <h1>社区闲置物资互助系统</h1>
      </div>
      <div class="form-container">
        <h2>用户注册</h2>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" size="large" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="registerForm.nickname" placeholder="请输入昵称" size="large" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" size="large" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）" size="large" />
          </el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form>
        <div class="register-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
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
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    const { confirmPassword, ...data } = registerForm
    await userStore.register(data)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  height: 100vh;
  width: 100vw;
  background: $bg-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-wrapper {
  width: 100%;
  max-width: 600px;
  text-align: center;

  .register-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    margin-bottom: 30px;
    h1 {
      font-size: 24px;
      color: $text-primary;
      font-weight: 600;
    }
  }

  .form-container {
    @include card-style;
    padding: 40px;
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 30px;
    }
    .register-btn {
      width: 100%;
      height: 46px;
      font-size: 16px;
      margin-top: 10px;
    }
    .register-footer {
      text-align: center;
      margin-top: 20px;
      color: $text-secondary;
    }
  }
}
</style>
