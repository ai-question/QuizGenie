import axios from 'axios'

// 创建axios实例
const api = axios.create({
  timeout: 150000,
  baseURL: '/api',
  headers: {
    'Accept': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
api.interceptors.response.use(response => {
  return response
}, error => {
  if (error.response?.status === 401) {
    // 未登录跳转到登录页
    window.location.href = '/login'
  }
  return Promise.reject(error)
})

export const quizApi = {
  // 获取题目集
  getQuestionSet(id) {
    return api.get(`/upload/sets/${id}`)
  },

  // 上传文件并创建题目集
  uploadFile(file, questionConfig) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('choiceCount', questionConfig.choiceCount)
    formData.append('judgmentCount', questionConfig.judgmentCount) 
    formData.append('shortAnswerCount', questionConfig.shortAnswerCount)
    
    // 对于文件上传，让浏览器自动处理 Content-Type 和 boundary
    return api.post('/upload', formData)
  },

  // 提交答案
  submitAnswers(setId, answers) {
    return api.post(`/upload/sets/${setId}/submit`, {
      answers
    })
  }
} 