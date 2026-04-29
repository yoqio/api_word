package com.apidoc.llm;

import com.apidoc.config.ApiDocConfig;
import com.apidoc.model.ApiEndpoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LlmService {
    
    private final ApiDocConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public LlmService(ApiDocConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateDescription(String codeSnippet, String context) {
        try {
            String prompt = buildDescriptionPrompt(codeSnippet, context);
            String response = callLlmApi(prompt);
            return extractContent(response);
        } catch (Exception e) {
            log.error("调用LLM生成描述失败", e);
            return context;
        }
    }
    
    public String generateExample(ApiEndpoint endpoint) {
        try {
            String prompt = buildExamplePrompt(endpoint);
            String response = callLlmApi(prompt);
            return extractContent(response);
        } catch (Exception e) {
            log.error("调用LLM生成示例失败", e);
            return "{}";
        }
    }
    
    public String generateErrorCodes(ApiEndpoint endpoint) {
        try {
            String prompt = buildErrorCodePrompt(endpoint);
            String response = callLlmApi(prompt);
            return extractContent(response);
        } catch (Exception e) {
            log.error("调用LLM生成错误码失败", e);
            return "[]";
        }
    }
    
    private String callLlmApi(String prompt) throws IOException {
        if (config.getLlm().getApiKey() == null || config.getLlm().getApiKey().isEmpty()) {
            log.warn("未配置API Key，跳过LLM调用");
            return "{\"choices\":[{\"message\":{\"content\":\"\"}}]}";
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getLlm().getModel());
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", new Object[]{message});
        requestBody.put("temperature", config.getLlm().getTemperature());
        requestBody.put("max_tokens", config.getLlm().getMaxTokens());
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getLlm().getApiUrl())
                .addHeader("Authorization", "Bearer " + config.getLlm().getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM API调用失败: " + response.code());
            }
            
            return response.body().string();
        }
    }
    
    private String buildDescriptionPrompt(String codeSnippet, String context) {
        return String.format(
            "请分析以下Java代码，生成简洁的中文描述：\n\n代码片段：\n%s\n\n上下文：%s\n\n请直接返回描述文本，不要包含其他解释。",
            codeSnippet, context
        );
    }
    
    private String buildExamplePrompt(ApiEndpoint endpoint) {
        return String.format(
            "请为以下API接口生成JSON格式的响应示例：\n\n" +
            "接口路径：%s %s\n" +
            "方法名称：%s\n" +
            "参数列表：%s\n" +
            "返回类型：%s\n\n" +
            "请直接返回JSON示例，不要包含其他解释。",
            endpoint.getHttpMethod(),
            endpoint.getPath(),
            endpoint.getMethodName(),
            endpoint.getParameters().toString(),
            endpoint.getResponseInfo().getType()
        );
    }
    
    private String buildErrorCodePrompt(ApiEndpoint endpoint) {
        return String.format(
            "请为以下API接口生成可能的错误码列表（JSON数组格式）：\n\n" +
            "接口路径：%s %s\n" +
            "方法名称：%s\n" +
            "功能描述：%s\n\n" +
            "格式要求：[{\"code\": \"错误码\", \"message\": \"错误信息\", \"description\": \"详细描述\"}]\n" +
            "请直接返回JSON数组，不要包含其他解释。",
            endpoint.getHttpMethod(),
            endpoint.getPath(),
            endpoint.getMethodName(),
            endpoint.getMethodDescription()
        );
    }
    
    private String extractContent(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    return message.get("content").asText().trim();
                }
            }
        } catch (Exception e) {
            log.error("解析LLM响应失败", e);
        }
        return "";
    }
}
