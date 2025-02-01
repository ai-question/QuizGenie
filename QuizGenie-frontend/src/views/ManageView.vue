<template>
  <div class="manage-container">
    <h2>题目管理</h2>
    <div class="manage-content">
      <div class="stats-cards">
        <div class="stat-card">
          <h3>总题目数</h3>
          <div class="stat-number">{{ stats.total }}</div>
        </div>
        <div class="stat-card">
          <h3>今日新增</h3>
          <div class="stat-number">{{ stats.today }}</div>
        </div>
      </div>
      
      <div class="question-sets">
        <h3>我的题目集</h3>
        <div class="sets-grid">
          <div 
            v-for="set in questionSets" 
            :key="set.id"
            class="set-card"
            @click="handleSetClick(set.id)"
          >
            <div class="set-info">
              <h4>{{ set.title }}</h4>
              <p class="description">{{ set.description }}</p>
              <div class="meta">
                <span>
                  <i class="fas fa-list"></i>
                  {{ set.questionCount }}题
                </span>
                <span>
                  <i class="fas fa-clock"></i>
                  {{ formatDate(set.createTime) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="action-buttons">
        <button class="action-btn primary" @click="handleCreateQuestion">
          <i class="fas fa-plus"></i>
          创建题目
        </button>
        <button class="action-btn" @click="handleImport">
          <i class="fas fa-file-import"></i>
          导入题目
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { quizApi } from '../api/quiz'
import { ElMessage } from 'element-plus'

const router = useRouter()
const stats = ref({
  total: 0,
  today: 0
})

const questionSets = ref([])

// 获取所有题目集
const fetchQuestionSets = async () => {
  try {
    const response = await quizApi.getAllQuestionSets()
    questionSets.value = response.data
  } catch (error) {
    ElMessage.error('获取题目集失败')
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 点击题目集跳转到答题页面
const handleSetClick = (setId) => {
  router.push(`/home/quiz/${setId}`)
}

const handleCreateQuestion = () => {
  // 创建题目的逻辑
}

const handleImport = () => {
  // 导入题目的逻辑
}

onMounted(() => {
  fetchQuestionSets()
})
</script>

<style scoped>
.manage-container {
  padding: 1rem;
}

h2 {
  color: #333;
  margin-bottom: 2rem;
}

.manage-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  color: #666;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.stat-number {
  color: #42a5f5;
  font-size: 2rem;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.action-btn {
  padding: 0.8rem 1.5rem;
  border: 1px solid #42a5f5;
  border-radius: 4px;
  background: white;
  color: #42a5f5;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.action-btn.primary {
  background: #42a5f5;
  color: white;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(66, 165, 245, 0.2);
}

.action-btn.primary:hover {
  background: #2196f3;
}

.question-sets {
  margin: 2rem 0;
}

.sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.set-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.set-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.set-info h4 {
  margin: 0 0 0.5rem;
  color: #1976d2;
  font-size: 1.1rem;
}

.description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  gap: 1rem;
  color: #888;
  font-size: 0.85rem;
}

.meta span {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.meta i {
  font-size: 0.9rem;
}
</style> 