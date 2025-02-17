package com.example.aiquiz.service;

import com.example.aiquiz.model.Question;
import com.example.aiquiz.dto.QuestionCountConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    private final String FILES_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/files";
    private final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<Question> generateQuestionsFromFile(MultipartFile file, String prompt) {
        try {
            // 1. 上传文件
            String fileId = uploadFile(file);
            
            // 2. 使用文件ID生成题目
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "fileid://" + fileId
            ));
            
            messages.add(Map.of(
                "role", "user",
                "content", prompt
            ));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            List<Question> questions = parseAIResponseToQuestions(response.getBody());
            if (questions.isEmpty()) {
                log.error("未能生成任何题目，AI响应：{}", response.getBody());
                throw new RuntimeException("未能生成题目");
            }
            return questions;
            
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
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
    
    private List<Question> parseAIResponseToQuestions(String aiResponse) {
        List<Question> questions = new ArrayList<>();
        try {
            // 从 AI 响应中提取实际内容
            JsonNode responseNode = objectMapper.readTree(aiResponse);
            String content = responseNode.get("choices")
                                       .get(0)
                                       .get("message")
                                       .get("content")
                                       .asText();
            
            // 按题目块分割（使用【】作为分隔符）
            String[] blocks = content.split("【");
            
            for (String block : blocks) {
                if (block.trim().isEmpty()) continue;
                
                Question question = parseQuestionBlock("【" + block);
                if (question != null) {
                    questions.add(question);
                }
            }
            
            // 添加日志以便调试
            log.debug("解析到 {} 道题目", questions.size());
            if (questions.size() == 0) {
                log.error("AI响应内容：{}", content);
            }
            
            return questions;
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw new RuntimeException("解析题目失败: " + e.getMessage());
        }
    }
    
    private Question parseQuestionBlock(String block) {
        try {
            // 提取题目类型
            String type;
            if (block.contains("选择题")) {
                type = "选择题";
            } else if (block.contains("判断题")) {
                type = "判断题";
            } else if (block.contains("简答题")) {
                type = "简答题";
            } else {
                return null;
            }
            
            String content;
            if (type.equals("选择题")) {
                // 提取题干和选项
                String rawContent = extractBetween(block, "题目：", "答案：").trim();
                String[] lines = rawContent.split("\n");
                
                // 构建格式化的内容：题干 + 选项
                StringBuilder contentBuilder = new StringBuilder();
                
                // 添加题干
                String questionText = lines[0].trim();
                contentBuilder.append(questionText).append("\n");
                
                // 查找并添加选项
                boolean[] optionFound = new boolean[4];
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.equals(questionText)) continue;
                    
                    // 检查是否是选项行（以A./B./C./D.开头）
                    if (line.matches("^[A-D]\\..+")) {
                        char optionKey = line.charAt(0);
                        int index = optionKey - 'A';
                        optionFound[index] = true;
                        contentBuilder.append(line).append("\n");
                    }
                }
                
                content = contentBuilder.toString().trim();
                
                // 验证是否包含所有选项
                for (char key = 'A'; key <= 'D'; key++) {
                    if (!content.contains(key + ".")) {
                        log.error("选择题格式错误，缺少选项 {}: {}", key, content);
                        return null;
                    }
                }
            } else {
                content = extractBetween(block, "题目：", "答案：").trim();
            }
            
            String answer = extractBetween(block, "答案：", "解析：").trim();
            String analysis = extractAfter(block, "解析：").trim();
            
            // 验证选择题答案格式
            if (type.equals("选择题") && !answer.matches("[A-D]")) {
                log.error("选择题答案格式错误: {}", answer);
                return null;
            }
            
            Question question = new Question();
            question.setType(type)
                    .setContent(content)
                    .setAnswer(answer)
                    .setAnalysis(analysis);
            
            return question;
        } catch (Exception e) {
            log.error("解析题目块失败: {}", block, e);
            return null;
        }
    }
    
    // 辅助方法：提取两个标记之间的文本
    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return "";
        startIndex += start.length();
        
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return text.substring(startIndex).trim();
        
        return text.substring(startIndex, endIndex).trim();
    }
    
    // 辅助方法：提取标记之后的所有文本
    private String extractAfter(String text, String start) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return "";
        startIndex += start.length();
        return text.substring(startIndex).trim();
    }
    
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
    
    public List<Question> generateQuestions(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", "你是一个专业的教育工作者，善于出题考核学生对知识的掌握程度。"));
            messages.add(createMessage("user", prompt));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-long");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            return parseAIResponseToQuestions(response.getBody());
            
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    public String generatePrompt(QuestionCountConfig config) {
        return String.format("""
            请根据文档内容生成题目，具体要求如下：
            1. %d道选择题（每题必须包含4个选项，选项格式为"A. 选项内容"）
            2. %d道判断题
            3. %d道简答题
            
            请严格按照以下格式输出每道题：
            
            【选择题】
            题目：<题干内容>
            A. <选项A内容>
            B. <选项B内容>
            C. <选项C内容>
            D. <选项D内容>
            答案：<A/B/C/D>
            解析：<解析内容>
            
            【判断题】
            题目：<题干内容>
            答案：<正确/错误>
            解析：<解析内容>
            
            【简答题】
            题目：<题干内容>
            答案：<答案内容>
            解析：<解析内容>
            
            注意：
            1. 选择题必须包含4个选项，每个选项必须以字母+点+空格开头（如"A. "）
            2. 选择题答案必须是A、B、C、D中的一个
            3. 判断题答案只能是"正确"或"错误"
            4. 每道题必须包含解析
            """, 
            config.getChoiceCount(),
            config.getJudgmentCount(),
            config.getShortAnswerCount()
        );
    }

    // 从解析文本中提取选项内容
    private String extractOptionFromAnalysis(char optionKey, String analysis) {
        String defaultTexts = switch (optionKey) {
            case 'A' -> "单链表必须存储在连续的内存空间中";
            case 'B' -> "单链表的每个节点有两个指针域";
            case 'C' -> "单链表的存储空间可以是不连续的";
            case 'D' -> "单链表的插入删除必须移动大量元素";
            default -> "";
        };
        
        // 尝试从解析中提取选项内容
        Pattern pattern = Pattern.compile("选项" + optionKey + "[^，。；]+[，。；]");
        Matcher matcher = pattern.matcher(analysis);
        if (matcher.find()) {
            String text = matcher.group()
                               .replace("选项" + optionKey, "")
                               .replaceAll("[，。；]$", "")
                               .replace("错误", "")
                               .replace("正确", "")
                               .trim();
            return text.isEmpty() ? defaultTexts : text;
        }
        
        return defaultTexts;
    }
} 