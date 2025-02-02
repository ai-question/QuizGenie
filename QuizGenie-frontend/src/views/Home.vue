<template>
  <div class="home-container">
    <header class="header">
      <div class="logo" @click="router.push('/home')">
        <img src="../assets/logo.png" alt="Logo">
        <span>QuizGenie</span>
      </div>
      <nav class="nav-tabs">
        <router-link 
          v-for="tab in tabs" 
          :key="tab.path"
          :to="tab.path"
          class="tab"
          :class="{ active: isActive(tab.path) }"
        >
          {{ tab.name }}
        </router-link>
      </nav>
      <div class="user-info">
        <span>{{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'
import { userApi } from '../api/user'

const router = useRouter()
const updateUsername = inject('updateUsername')
const username = inject('username')

const tabs = [
  { name: '首页', path: '/home' },
  { name: '在线答题', path: '/home/quiz' },
  { name: '题目管理', path: '/home/manage' },
  { name: '个人中心', path: '/home/profile' }
]

const handleLogout = async () => {
  try {
    await authApi.logout()
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    updateUsername('未登录')
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch (error) {
    ElMessage.error('退出登录失败')
  }
}

// 判断选项卡是否激活
const isActive = (path) => {
  if (path === '/home') {
    return router.currentRoute.value.path === '/home'
  }
  return router.currentRoute.value.path.startsWith(path)
}

// 更新用户名的方法
const updateLocalUsername = (newUsername) => {
  updateUsername(newUsername)
}

// 如果有需要，可以在组件挂载时获取最新的用户信息
const fetchUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.data.code === 200) {
      const newUsername = response.data.data.username
      updateLocalUsername(newUsername)
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 在组件挂载时获取用户信息
onMounted(() => {
  if (localStorage.getItem('token')) {
    fetchUserInfo()
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f6fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.logo img {
  height: 32px;
  width: auto;
}

.logo span {
  font-size: 1.2rem;
  font-weight: 600;
  color: #42a5f5;
}

.logo:hover span {
  color: #2196f3;
}

.nav-tabs {
  display: flex;
  gap: 2rem;
}

.tab {
  padding: 0.5rem 1rem;
  color: #666;
  text-decoration: none;
  font-weight: 500;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab:hover {
  color: #42a5f5;
}

.tab.active {
  color: #42a5f5;
  border-bottom-color: #42a5f5;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info span {
  color: #666;
}

.logout-btn {
  padding: 0.5rem 1rem;
  background-color: transparent;
  border: 1px solid #42a5f5;
  color: #42a5f5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #42a5f5;
  color: white;
}

.main-content {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* 添加过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 