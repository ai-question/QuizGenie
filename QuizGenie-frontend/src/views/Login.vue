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
          >
        </div>
        <div class="form-group">
          <input 
            type="password" 
            v-model="password" 
            placeholder="密码"
            required
          >
        </div>
        <button type="submit">登录</button>
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

const handleLogin = async () => {
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
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.login-box {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
</style> 