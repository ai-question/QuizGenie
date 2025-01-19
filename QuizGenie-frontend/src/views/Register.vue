<template>
  <div class="login-container">
    <div class="login-box">
      <h2>注册账号</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名"
            required
            @focus="inputFocus.username = true"
            @blur="inputFocus.username = false"
            :class="{ 'input-focus': inputFocus.username }"
          >
        </div>
        <div class="form-group">
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码"
            required
            @focus="inputFocus.password = true"
            @blur="inputFocus.password = false"
            :class="{ 'input-focus': inputFocus.password }"
          >
          <div class="password-strength" v-if="password">
            <div class="strength-text">密码强度：{{ passwordStrength }}</div>
            <div class="strength-bar">
              <div :class="['strength-indicator', passwordStrength]"></div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <input 
            type="password" 
            v-model="confirmPassword" 
            placeholder="请确认密码"
            required
            @focus="inputFocus.confirm = true"
            @blur="inputFocus.confirm = false"
            :class="{ 'input-focus': inputFocus.confirm }"
          >
        </div>
        <button type="submit" :disabled="loading || !isPasswordValid">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <div class="register-link">
        已有账号？<a href="#" @click.prevent="$router.push('/login')">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const inputFocus = ref({
  username: false,
  password: false,
  confirm: false
})

// 计算密码强度
const passwordStrength = computed(() => {
  const pwd = password.value
  if (!pwd) return ''
  
  let score = 0
  if (pwd.length >= 8) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[a-z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++
  
  if (score <= 2) return 'weak'
  if (score <= 3) return 'medium'
  return 'strong'
})

// 判断密码是否有效
const isPasswordValid = computed(() => {
  return password.value.length >= 6 && 
         password.value === confirmPassword.value
})

const handleRegister = async () => {
  if (!isPasswordValid.value) {
    ElMessage.error('请确保密码长度不少于6位且两次输入一致')
    return
  }
  
  loading.value = true
  try {
    await authApi.register(username.value, password.value)
    ElMessage.success('注册成功！')
    router.push('/login')
  } catch (error) {
    const errorMsg = error.response?.data?.message || '注册失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 复用 Login.vue 的基础样式 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to bottom, #42a5f5, #90caf9);
}

.login-box {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 420px;
}

h2 {
  text-align: center;
  margin-bottom: 2.5rem;
  color: #42a5f5;
  font-size: 1.8rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.5rem;
  position: relative;
}

input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e3f2fd;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

input:focus {
  outline: none;
  border-color: #42a5f5;
  box-shadow: 0 0 0 3px rgba(66, 165, 245, 0.1);
}

button {
  width: 100%;
  padding: 1rem;
  background-color: #42a5f5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-top: 1rem;
}

button:hover:not(:disabled) {
  background-color: #2196f3;
}

button:disabled {
  background-color: #bbdefb;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
  color: #666;
}

.register-link a {
  color: #42a5f5;
  text-decoration: none;
  margin-left: 0.5rem;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #2196f3;
  text-decoration: underline;
}

/* 密码强度指示器样式 */
.password-strength {
  margin-top: 0.5rem;
  font-size: 0.85rem;
}

.strength-text {
  color: #666;
  margin-bottom: 0.3rem;
}

.strength-bar {
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-indicator {
  height: 100%;
  width: 0;
  transition: all 0.3s ease;
}

.strength-indicator.weak {
  width: 33.33%;
  background-color: #ef5350;
}

.strength-indicator.medium {
  width: 66.66%;
  background-color: #ffa726;
}

.strength-indicator.strong {
  width: 100%;
  background-color: #66bb6a;
}

/* 输入验证提示 */
.form-group .validation-message {
  position: absolute;
  bottom: -20px;
  left: 0;
  font-size: 0.85rem;
  color: #ef5350;
}
</style> 