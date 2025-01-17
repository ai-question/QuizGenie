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
        <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const inputFocus = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await authApi.login(username.value, password.value)
    const { token, username: loginUsername } = response.data
    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginUsername)
      await router.push('/quiz')
      ElMessage.success(`欢迎回来，${loginUsername}！`)
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to bottom, #87CEEB, #B0E0E6); /* 天蓝色渐变背景 */
}

.login-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.2); /* 增加阴影效果 */
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  width: 100%;
  padding: 0.8rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #45a049;
}

.input-focus {
  border-color: #4CAF50; /* 聚焦时的边框颜色 */
}

button:disabled {
  background-color: #ccc; /* 禁用状态的按钮颜色 */
  cursor: not-allowed; /* 禁用状态的光标 */
}
</style> 