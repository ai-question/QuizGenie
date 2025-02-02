package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.example.aiquiz.model.QuestionSet;
import com.example.aiquiz.model.Question;
import com.example.aiquiz.mapper.QuestionSetMapper;
import com.example.aiquiz.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.example.aiquiz.dto.QuestionCountConfig;
import com.example.aiquiz.model.User;
import com.example.aiquiz.service.UserService;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class QuestionSetService {
    
    private static final Logger log = LoggerFactory.getLogger(QuestionSetService.class);
    private static final String FILES_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/files";
    private static final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    @Autowired
    private QuestionSetMapper questionSetMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private String uploadFile(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + apiKey);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("purpose", "file-extract");
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            FILES_URL,
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        return responseNode.get("id").asText();
    }
    
    @Transactional
    public QuestionSet createQuestionSet(MultipartFile file, QuestionCountConfig config) {
        try {
            // 获取当前登录用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 1. 从文件名生成标题
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                fileName = "未命名文件";
            }
            
            // 2. 先让AI生成文档描述
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "fileid://" + uploadFile(file)
            ));
            messages.add(Map.of(
                "role", "user",
                "content", "请用一段话简要描述这个文档的主要内容。"
            ));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // 解析文档描述
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            String description = responseNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText()
                .trim();
            
            // 4. 创建题目集合
            QuestionSet questionSet = new QuestionSet();
            questionSet.setUserId(currentUser.getId());
            questionSet.setTitle(fileName);
            questionSet.setDescription(description);
            questionSet.setQuestionCount(config.getChoiceCount() + 
                                       config.getJudgmentCount() + 
                                       config.getShortAnswerCount());
            LocalDateTime now = LocalDateTime.now();
            questionSet.setCreateTime(now);
            questionSet.setUpdateTime(now);
            
            questionSetMapper.insert(questionSet);
            
            // 添加日志
            log.debug("开始生成题目，配置：选择题{}道，判断题{}道，简答题{}道",
                     config.getChoiceCount(),
                     config.getJudgmentCount(),
                     config.getShortAnswerCount());
            
            List<Question> questions = aiService.generateQuestionsFromFile(
                file, 
                aiService.generatePrompt(config)
            );
            
            // 添加验证
            if (questions.size() != config.getChoiceCount() + 
                                  config.getJudgmentCount() + 
                                  config.getShortAnswerCount()) {
                log.error("生成的题目数量不正确，期望{}道，实际生成{}道",
                         config.getChoiceCount() + config.getJudgmentCount() + config.getShortAnswerCount(),
                         questions.size());
            }
            
            // 6. 保存题目
            int questionNumber = 1;
            for (Question question : questions) {
                question.setSetId(questionSet.getId())
                       .setQuestionNumber(questionNumber++);
                questionMapper.insert(question);
            }
            
            questionSet.setQuestions(questions);
            return questionSet;
            
        } catch (Exception e) {
            log.error("创建题目集失败", e);
            throw new RuntimeException("创建题目集失败: " + e.getMessage());
        }
    }
    
    public QuestionSet getQuestionSet(Long id) {
        // 获取当前登录用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 只能获取自己的题目集
        QuestionSet questionSet = questionSetMapper.findByIdAndUserId(id, currentUser.getId());
        if (questionSet == null) {
            throw new RuntimeException("题目集不存在或无权访问");
        }
        
        List<Question> questions = questionMapper.findBySetId(id);
        questionSet.setQuestions(questions);
        return questionSet;
    }
    
    public List<QuestionSet> getAllQuestionSets() {
        // 获取当前登录用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 只返回当前用户的题目集
        return questionSetMapper.findByUserId(currentUser.getId());
    }
    
    public Map<String, Object> getQuestionStats(String username) {
        // 获取用户ID
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取该用户的总题目集数量
        Integer total = questionSetMapper.countByUserId(user.getId());
        
        // 获取今日新增的题目集数量
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Integer today = questionSetMapper.countTodayByUserId(user.getId(), startOfDay);
        
        stats.put("total", total);
        stats.put("today", today);
        
        return stats;
    }
    
    public String exportQuestionSet(Long id) {
        QuestionSet questionSet = getQuestionSet(id);
        if (questionSet == null) {
            throw new RuntimeException("题目集不存在");
        }
        
        StringBuilder content = new StringBuilder();
        content.append("标题: ").append(questionSet.getTitle()).append("\n");
        content.append("描述: ").append(questionSet.getDescription()).append("\n\n");
        
        List<Question> questions = questionMapper.findBySetId(id);
        for (Question question : questions) {
            content.append("题号: ").append(question.getQuestionNumber()).append("\n");
            content.append("类型: ").append(question.getType()).append("\n");
            content.append("内容: ").append(question.getContent()).append("\n");
            content.append("答案: ").append(question.getAnswer()).append("\n");
            content.append("解析: ").append(question.getAnalysis()).append("\n\n");
        }
        
        return content.toString();
    }
    
    @Transactional
    public QuestionSet importQuestionSet(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        
        if (lines.length < 2) {
            throw new RuntimeException("文件格式不正确");
        }
        
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QuestionSet questionSet = new QuestionSet();
        questionSet.setUserId(currentUser.getId());
        
        try {
            String title = lines[0].substring(4).trim();
            String description = lines[1].substring(4).trim();
            
            // 验证标题和描述不为空
            if (title.isEmpty() || description.isEmpty()) {
                throw new RuntimeException("标题或描述不能为空");
            }
            
            questionSet.setTitle(title);
            questionSet.setDescription(description);
            questionSet.setCreateTime(LocalDateTime.now());
            questionSet.setUpdateTime(LocalDateTime.now());
            questionSet.setQuestionCount(0);  // 初始化题目数量为0
            
            questionSetMapper.insert(questionSet);
            
            // 解析题目
            List<Question> questions = parseQuestionsFromContent(lines, questionSet.getId());
            if (questions.isEmpty()) {
                throw new RuntimeException("未找到有效题目");
            }
            
            // 更新题目数量
            questionSet.setQuestionCount(questions.size());
            questionSetMapper.updateQuestionCount(questionSet.getId(), questions.size());
            
            questionSet.setQuestions(questions);
            
            // 更新统计数据（可选，因为数据库查询会自动反映最新数据）
            Map<String, Object> stats = getQuestionStats(currentUser.getUsername());
            
            return questionSet;
            
        } catch (Exception e) {
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("导入失败：" + e.getMessage());
        }
    }
    
    private List<Question> parseQuestionsFromContent(String[] lines, Long setId) {
        List<Question> questions = new ArrayList<>();
        Question currentQuestion = null;
        
        for (int i = 3; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("题号:")) {
                if (currentQuestion != null) {
                    questions.add(currentQuestion);
                }
                currentQuestion = new Question();
                currentQuestion.setSetId(setId);
                currentQuestion.setQuestionNumber(Integer.parseInt(line.substring(3).trim()));
            } else if (currentQuestion != null) {
                if (line.startsWith("类型:")) {
                    currentQuestion.setType(line.substring(3).trim());
                } else if (line.startsWith("内容:")) {
                    currentQuestion.setContent(line.substring(3).trim());
                } else if (line.startsWith("答案:")) {
                    currentQuestion.setAnswer(line.substring(3).trim());
                } else if (line.startsWith("解析:")) {
                    currentQuestion.setAnalysis(line.substring(3).trim());
                }
            }
        }
        
        if (currentQuestion != null) {
            questions.add(currentQuestion);
        }
        
        // 保存所有题目
        for (Question question : questions) {
            questionMapper.insert(question);
        }
        
        return questions;
    }
} 