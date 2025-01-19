<template>
  <div class="dashboard-container">
    <h2>欢迎使用 QuizGenie</h2>
    
    <div class="feature-cards">
      <div class="feature-card" @click="router.push('/home/quiz')">
        <div class="card-icon">
          <i class="fas fa-pencil-alt"></i>
        </div>
        <h3>在线答题</h3>
        <p>开始答题，提升能力</p>
      </div>
      
      <div class="feature-card" @click="router.push('/home/manage')">
        <div class="card-icon">
          <i class="fas fa-tasks"></i>
        </div>
        <h3>题目管理</h3>
        <p>管理和创建题目</p>
      </div>
      
      <div class="feature-card" @click="router.push('/home/profile')">
        <div class="card-icon">
          <i class="fas fa-user"></i>
        </div>
        <h3>个人中心</h3>
        <p>查看个人信息和统计</p>
      </div>
    </div>
    
    <div class="stats-section">
      <div class="stat-item">
        <div class="stat-label">总答题数</div>
        <div class="stat-value">{{ stats.totalQuestions }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">正确率</div>
        <div class="stat-value">{{ stats.accuracy }}%</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">连续答题</div>
        <div class="stat-value">{{ stats.streak }} 天</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = ref({
  totalQuestions: 0,
  accuracy: 0,
  streak: 0
})
</script>

<style scoped>
.dashboard-container {
  padding: 2rem;
}

h2 {
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(66, 165, 245, 0.2);
}

.card-icon {
  width: 60px;
  height: 60px;
  background: #e3f2fd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.card-icon i {
  font-size: 24px;
  color: #42a5f5;
}

.feature-card h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.feature-card p {
  color: #666;
  font-size: 0.9rem;
}

.stats-section {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin-top: 2rem;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  color: #42a5f5;
  font-size: 2rem;
  font-weight: 600;
}
</style> 