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
            <div class="stat-value">{{ userStats.questionSets }}</div>
            <div class="stat-label">生成题目集数</div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="30%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="30%">
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const updateUsername = inject('updateUsername')
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
  questionSets: 0
})

const editDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)

const editForm = ref({
  username: '',
  email: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

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

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const response = await userApi.getUserStats()
    if (response.data) {
      userStats.value = {
        totalQuestions: response.data.totalQuestions || 0,
        correctRate: response.data.correctRate || 0,
        streak: response.data.streak || 0,
        questionSets: response.data.questionSets || 0
      }
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
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
  fetchUserStats()
})

const handleEdit = () => {
  editForm.value.username = userInfo.value.username
  editForm.value.email = userInfo.value.email
  editDialogVisible.value = true
}

const handleChangePassword = () => {
  passwordDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    const response = await userApi.updateUserInfo(editForm.value)
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      if (editForm.value.username) {
        localStorage.setItem('username', editForm.value.username)
        updateUsername(editForm.value.username)
      }
      // 获取新token并更新
      const newToken = response.headers['authorization']
      if (newToken) {
        localStorage.setItem('token', newToken.replace('Bearer ', ''))
      }
      fetchUserInfo()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await userApi.updatePassword(passwordForm.value)
        if (response.data.code === 200) {
          ElMessage.success('密码修改成功')
          passwordDialogVisible.value = false
          passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
          // 清除本地存储的登录信息
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          // 跳转到登录页
          router.push('/login')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      }
    }
  })
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