<template>
  <div class="question-card">
    <div class="question-header">
      <span class="question-type">{{ getQuestionTypeText }}</span>
      <span class="question-number">第 {{ question.questionNumber }} 题</span>
    </div>
    
    <div class="question-content">{{ getQuestionContent }}</div>
    
    <!-- 选择题 -->
    <div v-if="question.type === '选择题'" class="options">
      <div 
        v-for="option in options" 
        :key="option.key"
        class="option"
        :class="{ 
          'selected': userAnswer === option.key,
          'correct': showAnswer && option.key === question.answer,
          'wrong': showAnswer && userAnswer === option.key && option.key !== question.answer
        }"
        @click="selectAnswer(option.key)"
      >
        <span class="option-text">{{ option.text }}</span>
      </div>
    </div>
    
    <!-- 判断题 -->
    <div v-else-if="question.type === '判断题'" class="judgment-options">
      <button 
        class="judgment-btn"
        :class="{ 
          'selected': userAnswer === '正确',
          'correct': showAnswer && question.answer === '正确',
          'wrong': showAnswer && userAnswer === '正确' && question.answer !== '正确'
        }"
        @click="selectAnswer('正确')"
      >
        正确
      </button>
      <button 
        class="judgment-btn"
        :class="{ 
          'selected': userAnswer === '错误',
          'correct': showAnswer && question.answer === '错误',
          'wrong': showAnswer && userAnswer === '错误' && question.answer !== '错误'
        }"
        @click="selectAnswer('错误')"
      >
        错误
      </button>
    </div>
    
    <!-- 简答题 -->
    <div v-else-if="question.type === '简答题'" class="short-answer">
      <textarea
        v-model="userAnswer"
        placeholder="请输入你的答案..."
        :disabled="showAnswer"
        @input="selectAnswer($event.target.value)"
      ></textarea>
    </div>
    
    <!-- 答案解析 -->
    <div v-if="showAnswer" class="answer-analysis">
      <div class="correct-answer">
        <strong>正确答案：</strong>{{ question.answer }}
      </div>
      <div class="analysis" v-if="question.analysis">
        <strong>解析：</strong>
        <p>{{ question.analysis }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'

export default {
  name: 'QuestionCard',
  
  props: {
    question: {
      type: Object,
      required: true
    },
    showAnswer: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['answer-selected'],
  
  setup(props, { emit }) {
    const userAnswer = ref('')
    
    // 计算题目类型显示文本
    const getQuestionTypeText = computed(() => {
      return props.question.type || '未知类型'
    })
    
    // 获取处理后的题目内容
    const getQuestionContent = computed(() => {
      return props.question.content || ''
    })
    
    // 解析选择题选项
    const options = computed(() => {
      if (props.question.type !== '选择题') return []
      
      // 选择题选项固定为 A, B, C, D，但需要添加选项内容
      return [
        { key: 'A', text: 'A. 单链表的存储空间必须是连续的' },
        { key: 'B', text: 'B. 单链表的节点大小必须相同' },
        { key: 'C', text: 'C. 单链表的存储空间是动态分配的，不要求连续' },
        { key: 'D', text: 'D. 单链表的存储空间必须在编译时确定' }
      ]
    })
    
    // 选择答案
    const selectAnswer = (answer) => {
      userAnswer.value = answer
      emit('answer-selected', {
        questionId: props.question.id,
        answer: answer
      })
    }

    // 判断题答案显示转换
    const getJudgmentAnswer = computed(() => {
      if (props.question.type !== '判断题') return ''
      return props.question.answer === '正确' ? 'true' : 'false'
    })
    
    return {
      userAnswer,
      getQuestionTypeText,
      getQuestionContent,
      options,
      selectAnswer,
      getJudgmentAnswer
    }
  }
}
</script>

<style scoped>
.question-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.question-type {
  background: #1976d2;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.question-number {
  color: #666;
}

.question-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  white-space: pre-line;
}

/* 选择题样式 */
.options .option {
  display: flex;
  align-items: center;
  padding: 12px;
  margin: 8px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.option:hover {
  background: #f5f5f5;
}

.option.selected {
  background: #e3f2fd;
  border-color: #1976d2;
}

.option.correct {
  background: #c8e6c9;
  border-color: #4caf50;
}

.option.wrong {
  background: #ffcdd2;
  border-color: #f44336;
}

/* 判断题样式 */
.judgment-options {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.judgment-btn {
  padding: 10px 30px;
  border: 2px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.judgment-btn:hover {
  background: #f5f5f5;
}

.judgment-btn.selected {
  background: #e3f2fd;
  border-color: #1976d2;
}

/* 简答题样式 */
.short-answer textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

/* 答案解析样式 */
.answer-analysis {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.correct-answer {
  margin-bottom: 12px;
  color: #4caf50;
}

.analysis {
  color: #666;
}

.analysis p {
  margin: 8px 0;
  line-height: 1.6;
}

/* 修改选项样式 */
.options .option {
  display: flex;
  align-items: center;
  padding: 12px;
  margin: 8px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-text {
  flex: 1;
  margin-left: 8px;
}
</style>