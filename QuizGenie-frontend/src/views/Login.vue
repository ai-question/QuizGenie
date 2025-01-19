<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <input 
            type="text" 
            v-model="username" 
            placeholder="用户名"
            required
            @focus="inputFocus = true"
            @blur="inputFocus = false"
            :class="{ 'input-focus': inputFocus }"
          >
        </div>
        <div class="form-group">
          <input 
            type="password" 
            v-model="password" 
            placeholder="密码"
            required
            @focus="inputFocus = true"
            @blur="inputFocus = false"
            :class="{ 'input-focus': inputFocus }"
          >
        </div>
        <div class="form-footer">
          <label class="remember-password">
            <input 
              type="checkbox" 
              v-model="rememberMe"
            >
            <span>记住密码</span>
          </label>
          <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
        </div>
        <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
      <div class="register-link">
        还没有账号？<a href="#" @click.prevent="handleRegister">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const inputFocus = ref(false)
const rememberMe = ref(false)

onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  const savedPassword = localStorage.getItem('savedPassword')
  if (savedUsername && savedPassword) {
    username.value = savedUsername
    password.value = savedPassword
    rememberMe.value = true
  }
})

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await authApi.login(username.value, password.value)
    const { token, username: loginUsername } = response.data
    if (token) {
      if (rememberMe.value) {
        localStorage.setItem('savedUsername', username.value)
        localStorage.setItem('savedPassword', password.value)
      } else {
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
      }
      
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginUsername)
      await router.push('/home')
      ElMessage.success(`欢迎回来，${loginUsername}！`)
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能开发中...')
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to bottom, #42a5f5, #90caf9); /* 更浅的蓝色渐变背景 */
}

.login-box {
  background: white;
  padding: 2.5rem;  /* 增加内边距 */
  border-radius: 12px; /* 增大圆角 */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15); /* 优化阴影效果 */
  width: 100%;
  max-width: 420px;
}

h2 {
  text-align: center;
  margin-bottom: 2.5rem;
  color: #42a5f5; /* 更浅的蓝色 */
  font-size: 1.8rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 1rem;
}

input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e3f2fd; /* 保持淡蓝色边框 */
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

input:focus {
  outline: none;
  border-color: #42a5f5; /* 更浅的蓝色 */
  box-shadow: 0 0 0 3px rgba(66, 165, 245, 0.1); /* 调整阴影颜色 */
}

button {
  width: 100%;
  padding: 1rem;
  background-color: #42a5f5; /* 更浅的蓝色 */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #2196f3; /* 悬停时稍深一点的蓝色 */
}

button:disabled {
  background-color: #bbdefb; /* 更浅的禁用状态蓝色 */
  cursor: not-allowed;
}

.input-focus {
  border-color: #4CAF50; /* 聚焦时的边框颜色 */
}

button:disabled {
  background-color: #ccc; /* 禁用状态的按钮颜色 */
  cursor: not-allowed; /* 禁用状态的光标 */
}

/* 添加记住密码和忘记密码的样式 */
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 1rem 0;  /* 上下增加间距 */
  font-size: 0.9rem;
}

.remember-password {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  cursor: pointer;
}

.remember-password input[type="checkbox"] {
  width: auto;  /* 覆盖之前的 100% 宽度 */
  padding: 0;   /* 移除之前的 padding */
  margin: 0;    /* 移除默认边距 */
  cursor: pointer;
}

.forgot-password {
  color: #42a5f5; /* 更浅的蓝色 */
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #2196f3; /* 悬停时稍深一点的蓝色 */
}

/* 添加注册链接样式 */
.register-link {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
  color: #666;
}

.register-link a {
  color: #42a5f5; /* 更浅的蓝色 */
  text-decoration: none;
  margin-left: 0.5rem;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #2196f3; /* 悬停时稍深一点的蓝色 */
}

/* 调整按钮与表单底部的间距 */
form button {
  margin-top: 1rem;  /* 增加与上方元素的间距 */
}

/* 优化链接样式 */
.forgot-password, .register-link a {
  color: #42a5f5;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover, .register-link a:hover {
  color: #2196f3;
  text-decoration: underline;
}
</style> 