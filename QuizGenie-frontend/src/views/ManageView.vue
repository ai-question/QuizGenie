<template>
  <div class="manage-container">
    <h2>题目管理</h2>
    <div class="manage-content">
      <div class="stats-container">
        <div class="stat-item">
          <h3>总题目集</h3>
          <p>{{ stats.total }}</p>
        </div>
        <div class="stat-item">
          <h3>今日新增</h3>
          <p>{{ stats.today }}</p>
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
                <button 
                  class="export-btn"
                  @click.stop="handleExport(set.id)"
                  title="导出题目集"
                >
                  <i class="fas fa-download"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="action-buttons">
        <button class="action-btn primary" @click="handleCreateQuestion">
          <i class="fas fa-plus"></i>
          新建题目集
        </button>
        <button class="action-btn" @click="handleImport">
          <i class="fas fa-file-import"></i>
          导入题目
        </button>
      </div>
    </div>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入题目集"
      width="30%"
    >
      <div class="import-dialog">
        <input
          type="file"
          ref="importFileInput"
          @change="handleImportFileChange"
          accept=".txt"
          style="display: none"
        >
        <div 
          class="import-drop-zone"
          @click="triggerImportFile"
          @drop.prevent="handleFileDrop"
          @dragover.prevent
        >
          <i class="fas fa-cloud-upload-alt"></i>
          <p>点击或拖拽文件到此处</p>
          <span>支持 .txt 格式的题目集文件</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { quizApi } from '../api/quiz'
import { ElMessage, ElDialog } from 'element-plus'

const router = useRouter()
const stats = ref({
  total: 0,
  today: 0
})

const questionSets = ref([])

const importDialogVisible = ref(false)
const importFileInput = ref(null)

// 获取所有题目集
const fetchQuestionSets = async () => {
  try {
    const response = await quizApi.getAllQuestionSets()
    questionSets.value = response.data
  } catch (error) {
    ElMessage.error('获取题目集失败')
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await quizApi.getQuestionStats()
    stats.value = response.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
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
  // 跳转到在线答题页面
  router.push('/home/quiz')
}

// 导出题目集
const handleExport = async (setId) => {
  try {
    const response = await quizApi.exportQuestionSet(setId)
    const blob = new Blob([response.data], { type: 'text/plain' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `题目集_${setId}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 触发文件选择
const triggerImportFile = () => {
  importFileInput.value.click()
}

// 处理文件选择
const handleImportFileChange = async (event) => {
  const file = event.target.files[0]
  if (file) {
    await importFile(file)
  }
}

// 处理文件拖放
const handleFileDrop = async (event) => {
  const file = event.dataTransfer.files[0]
  if (file) {
    await importFile(file)
  }
}

// 导入文件
const importFile = async (file) => {
  try {
    if (!file.name.endsWith('.txt')) {
      ElMessage.error('只支持导入 .txt 格式的题目集文件')
      return
    }
    
    await quizApi.importQuestionSet(file)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    fetchQuestionSets()
    await fetchStats()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '导入失败，请检查文件格式是否正确')
    console.error('导入错误:', error)
  }
}

// 定时刷新统计数据
let refreshInterval
const startAutoRefresh = () => {
  refreshInterval = setInterval(fetchStats, 60000) // 每分钟刷新一次
}

onMounted(async () => {
  await fetchQuestionSets()
  await fetchStats()
  startAutoRefresh()
})

onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
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

.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  flex: 1;
}

.stat-item h3 {
  color: #666;
  font-size: 16px;
  margin: 0 0 10px 0;
}

.stat-item p {
  color: #409EFF;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
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

.export-btn {
  background: transparent;
  border: none;
  color: #409EFF;
  cursor: pointer;
  padding: 4px 8px;
  transition: all 0.3s;
}

.export-btn:hover {
  color: #66b1ff;
}

.import-dialog {
  padding: 20px;
}

.import-drop-zone {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.import-drop-zone:hover {
  border-color: #409EFF;
}

.import-drop-zone i {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.import-drop-zone p {
  margin: 8px 0;
  font-size: 16px;
  color: #606266;
}

.import-drop-zone span {
  font-size: 14px;
  color: #909399;
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