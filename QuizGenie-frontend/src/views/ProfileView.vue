<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar">
          <img :src="userInfo.avatar || defaultAvatar" alt="用户头像">
        </div>
        <input
          type="file"
          ref="avatarInput"
          accept="image/*"
          style="display: none"
          @change="handleAvatarUpload"
        >
        <button class="upload-btn" @click="$refs.avatarInput.click()">
          <i class="fas fa-camera"></i>
          更换头像
        </button>
      </div>
      <div class="user-info">
        <h2>{{ userInfo.username }}</h2>
        <p class="join-date">加入时间：{{ userInfo.joinDate }}</p>
      </div>
    </div>

    <div class="profile-content">
      <div class="info-card">
        <h3>基本信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ userInfo.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机</span>
            <span class="value">{{ userInfo.phone || '未设置' }}</span>
          </div>
        </div>
        <div class="actions">
          <button class="edit-btn" @click="handleEdit">
            <i class="fas fa-edit"></i>
            编辑资料
          </button>
          <button class="change-pwd-btn" @click="handleChangePassword">
            <i class="fas fa-key"></i>
            修改密码
          </button>
        </div>
      </div>

      <div class="stats-card">
        <h3>答题统计</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ userStats.totalQuestions }}</div>
            <div class="stat-label">总答题数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.correctRate }}%</div>
            <div class="stat-label">正确率</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.streak }}</div>
            <div class="stat-label">连续答题天数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.rank }}</div>
            <div class="stat-label">排名</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'

const defaultAvatar = 'https://via.placeholder.com/100'

const userInfo = ref({
  username: localStorage.getItem('username') || '未登录',
  avatar: '',
  email: '',
  phone: '',
  joinDate: ''
})

const userStats = ref({
  totalQuestions: 0,
  correctRate: 0,
  streak: 0,
  rank: '-'
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.data.code === 200) {
      userInfo.value = {
        ...userInfo.value,
        ...response.data.data
      }
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 处理头像上传
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }
  
  // 验证文件大小 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  try {
    const response = await userApi.uploadAvatar(file)
    if (response.data.code === 200) {
      userInfo.value.avatar = response.data.data.avatarUrl
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  fetchUserInfo()
})

const handleEdit = () => {
  ElMessage.info('编辑资料功能开发中...')
}

const handleChangePassword = () => {
  ElMessage.info('修改密码功能开发中...')
}
</script>

<style scoped>
.profile-container {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #42a5f5;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #42a5f5;
  color: #42a5f5;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background: #42a5f5;
  color: white;
}

.user-info h2 {
  color: #333;
  margin-bottom: 0.5rem;
}

.join-date {
  color: #666;
  font-size: 0.9rem;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.info-card, .stats-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h3 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #eee;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-weight: 500;
}

.actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
}

.edit-btn, .change-pwd-btn {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.edit-btn {
  background: #42a5f5;
  color: white;
}

.change-pwd-btn {
  background: #f5f5f5;
  color: #666;
}

.edit-btn:hover {
  background: #2196f3;
}

.change-pwd-btn:hover {
  background: #e0e0e0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.stats-grid .stat-item {
  text-align: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.stats-grid .stat-value {
  color: #42a5f5;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.stats-grid .stat-label {
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
}
</style> 