# QuizGenie API 文档

## 基础信息
- 基础URL: `/api`
- 所有请求都需要在 header 中携带 token（登录接口除外）
  ```
  Authorization: Bearer <token>
  ```
- 响应格式：
  ```json
  {
    "code": 200,      // 状态码
    "message": "",    // 提示信息
    "data": {}        // 响应数据
  }
  ```

## 1. 用户认证

### 1.1 用户登录
- **接口**: `/login`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "username": "string",  // 用户名
    "password": "string"   // 密码
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "string",      // JWT token
      "username": "string",   // 用户名
      "avatar": "string"      // 头像URL
    }
  }
  ```

### 1.2 用户注册
- **接口**: `/register`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "username": "string",  // 用户名
    "password": "string"   // 密码
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "username": "string"
    }
  }
  ```

### 1.3 退出登录
- **接口**: `/logout`
- **方法**: `POST`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "退出成功"
  }
  ```

## 2. 用户信息

### 2.1 获取用户信息
- **接口**: `/user/info`
- **方法**: `GET`
- **响应**:
  ```json
  {
    "code": 200,
    "data": {
      "username": "string",    // 用户名
      "avatar": "string",      // 头像URL
      "email": "string",       // 邮箱
      "phone": "string",       // 手机号
      "joinDate": "string",    // 注册时间
      "stats": {
        "totalQuestions": 0,   // 总答题数
        "correctRate": 0,      // 正确率
        "streak": 0,           // 连续答题天数
        "rank": 0             // 排名
      }
    }
  }
  ```

### 2.2 更新用户信息
- **接口**: `/user/update`
- **方法**: `PUT`
- **请求体**:
  ```json
  {
    "email": "string",    // 可选
    "phone": "string",    // 可选
    "avatar": "string"    // 可选，头像URL
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "更新成功"
  }
  ```

### 2.3 修改密码
- **接口**: `/user/password`
- **方法**: `PUT`
- **请求体**:
  ```json
  {
    "oldPassword": "string",
    "newPassword": "string"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "密码修改成功"
  }
  ```

## 3. 题目管理

### 3.1 获取题目列表
- **接口**: `/questions`
- **方法**: `GET`
- **参数**:
  ```
  page: number       // 页码，默认1
  size: number       // 每页数量，默认10
  type: string       // 题目类型，可选
  difficulty: string // 难度，可选
  keyword: string    // 搜索关键词，可选
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "data": {
      "total": 0,
      "items": [{
        "id": "string",
        "title": "string",
        "type": "string",
        "difficulty": "string",
        "createTime": "string",
        "updateTime": "string"
      }]
    }
  }
  ```

### 3.2 创建题目
- **接口**: `/questions`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "title": "string",          // 题目标题
    "content": "string",        // 题目内容
    "type": "string",          // 题目类型
    "difficulty": "string",    // 难度
    "options": [               // 选项（选择题必填）
      {
        "content": "string",
        "isCorrect": boolean
      }
    ],
    "answer": "string",        // 答案
    "explanation": "string"    // 解析
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "创建成功",
    "data": {
      "id": "string"
    }
  }
  ```

### 3.3 获取题目统计
- **接口**: `/questions/stats`
- **方法**: `GET`
- **响应**:
  ```json
  {
    "code": 200,
    "data": {
      "total": 0,      // 总题目数
      "today": 0,      // 今日新增
      "byType": {      // 按类型统计
        "choice": 0,
        "completion": 0
      },
      "byDifficulty": {  // 按难度统计
        "easy": 0,
        "medium": 0,
        "hard": 0
      }
    }
  }
  ```

## 4. 答题功能

### 4.1 开始答题
- **接口**: `/quiz/start`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "type": "string",      // 题目类型，可选
    "difficulty": "string", // 难度，可选
    "count": 10           // 题目数量
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "data": {
      "quizId": "string",
      "questions": [{
        "id": "string",
        "title": "string",
        "content": "string",
        "type": "string",
        "options": [{
          "id": "string",
          "content": "string"
        }]
      }]
    }
  }
  ```

### 4.2 提交答案
- **接口**: `/quiz/submit`
- **方法**: `POST`
- **请求体**:
  ```json
  {
    "quizId": "string",
    "answers": [{
      "questionId": "string",
      "answer": "string"
    }]
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "data": {
      "score": 0,           // 得分
      "correctCount": 0,    // 正确题数
      "totalCount": 0,      // 总题数
      "details": [{
        "questionId": "string",
        "isCorrect": boolean,
        "correctAnswer": "string",
        "explanation": "string"
      }]
    }
  }
  ```

## 5. 错误码说明

- 200: 成功
- 400: 请求参数错误
- 401: 未授权（未登录或token失效）
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

## 6. 注意事项

1. 所有请求都需要在header中携带token（登录注册除外）
2. 文件上传接口需要使用multipart/form-data格式
3. 日期时间格式统一使用ISO 8601标准
4. 分页接口默认每页10条数据
5. 建议在请求时设置合适的超时时间 