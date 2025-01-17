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
import { ref } from 'vue'

const stats = ref({
  total: 0,
  today: 0
})

const handleCreateQuestion = () => {
  // 创建题目的逻辑
}

const handleImport = () => {
  // 导入题目的逻辑
}
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
</style> 