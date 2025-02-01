<template>
  <div class="question-card">
    <div class="question-header">
      <span class="question-number">第 {{ question.questionNumber }} 题</span>
      <span class="question-type">{{ question.type }}</span>
    </div>
    
    <div class="question-content">
      <h3>{{ question.content }}</h3>
      
      <!-- 选择题 -->
      <div v-if="question.type === '选择题'" class="options">
        <div
          v-for="option in options"
          :key="option.key"
          class="option"
          :class="{ 
            'selected': answer === option.key,
            'correct': showAnswer && option.key === question.answer,
            'wrong': showAnswer && answer === option.key && option.key !== question.answer
          }"
          @click="!showAnswer && selectAnswer(option.key)"
        >
          <span class="option-text">{{ option.text }}</span>
        </div>
      </div>
      
      <!-- 判断题 -->
      <div v-else-if="question.type === '判断题'" class="judgment-options">
        <div
          v-for="option in judgmentOptions"
          :key="option.key"
          class="option"
          :class="{
            'selected': answer === option.key,
            'correct': showAnswer && option.key === question.answer,
            'wrong': showAnswer && answer === option.key && option.key !== question.answer
          }"
          @click="!showAnswer && selectAnswer(option.key)"
        >
          {{ option.text }}
        </div>
      </div>
      
      <!-- 简答题 -->
      <div v-else-if="question.type === '简答题'" class="text-answer">
        <textarea
          v-model="answer"
          :disabled="showAnswer"
          @input="selectAnswer($event.target.value)"
          placeholder="请输入你的答案..."
        ></textarea>
      </div>
    </div>
    
    <!-- 显示答案和解析 -->
    <div v-if="showAnswer" class="answer-analysis">
      <div class="correct-answer">
        <strong>正确答案：</strong>
        <span>{{ question.answer }}</span>
      </div>
      <div v-if="question.analysis" class="analysis">
        <strong>解析：</strong>
        <p>{{ question.analysis }}</p>
      </div>
    </div>
  </div>
</template>

<script>
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
  
  data() {
    return {
      answer: '',
      options: [
        { key: 'A', text: 'A. 单链表必须存储在连续的内存空间中' },
        { key: 'B', text: 'B. 单链表的存储空间可以是不连续的' },
        { key: 'C', text: 'C. 单链表的每个节点有两个指针域' },
        { key: 'D', text: 'D. 单链表的插入删除必须移动大量元素' }
      ],
      judgmentOptions: [
        { key: '正确', text: '正确' },
        { key: '错误', text: '错误' }
      ]
    }
  },
  
  methods: {
    selectAnswer(value) {
      this.answer = value
      this.$emit('answer-selected', {
        questionId: this.question.id,
        answer: value
      })
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

.question-number {
  color: #666;
}

.question-type {
  background: #1976d2;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
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

.option:hover:not(.selected):not(.correct):not(.wrong) {
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
.text-answer textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  font-size: 14px;
  line-height: 1.6;
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