package com.apidoc.generator;

import com.apidoc.config.ApiDocConfig;
import com.apidoc.llm.LlmService;
import com.apidoc.model.ApiDocumentation;
import com.apidoc.model.ApiEndpoint;
import com.apidoc.scanner.ControllerScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DocumentGenerator {
    
    private final ControllerScanner scanner;
    private final LlmService llmService;
    private final ApiDocConfig config;
    private final ObjectMapper objectMapper;
    
    public DocumentGenerator(ControllerScanner scanner, LlmService llmService, ApiDocConfig config) {
        this.scanner = scanner;
        this.llmService = llmService;
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public ApiDocumentation generateDocumentation() {
        log.info("开始生成接口文档...");
        
        List<ApiEndpoint> endpoints = scanner.scanControllers();
        
        enrichEndpoints(endpoints);
        
        ApiDocumentation documentation = ApiDocumentation.builder()
                .projectName("API Documentation")
                .version("1.0.0")
                .generatedTime(System.currentTimeMillis())
                .endpoints(endpoints)
                .totalEndpoints(endpoints.size())
                .coveredEndpoints(endpoints.size())
                .coverageRate(endpoints.isEmpty() ? 0 : 100.0)
                .build();
        
        saveDocumentation(documentation);
        
        log.info("接口文档生成完成，共 {} 个端点", endpoints.size());
        
        return documentation;
    }
    
    private void enrichEndpoints(List<ApiEndpoint> endpoints) {
        for (ApiEndpoint endpoint : endpoints) {
            try {
                if (endpoint.getMethodDescription().equals(endpoint.getMethodName())) {
                    String enhancedDesc = llmService.generateDescription(
                        endpoint.getMethodName(), 
                        endpoint.getClassName()
                    );
                    if (!enhancedDesc.isEmpty()) {
                        endpoint.setMethodDescription(enhancedDesc);
                    }
                }
                
                String example = llmService.generateExample(endpoint);
                endpoint.setResponseBodyExample(example);
                
                log.debug("增强接口信息完成: {} {}", endpoint.getHttpMethod(), endpoint.getPath());
            } catch (Exception e) {
                log.error("增强接口信息失败: {} {}", endpoint.getHttpMethod(), endpoint.getPath(), e);
            }
        }
    }
    
    private void saveDocumentation(ApiDocumentation documentation) {
        try {
            Path outputDir = Paths.get(config.getOutputDir());
            Files.createDirectories(outputDir);
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            if ("markdown".equalsIgnoreCase(config.getDocFormat())) {
                saveAsMarkdown(documentation, outputDir, timestamp);
            }
            
            saveAsJson(documentation, outputDir, timestamp);
            
            log.info("文档已保存到: {}", outputDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("保存文档失败", e);
        }
    }
    
    private void saveAsMarkdown(ApiDocumentation documentation, Path outputDir, String timestamp) throws IOException {
        StringBuilder md = new StringBuilder();
        
        md.append("# ").append(documentation.getProjectName()).append("\n\n");
        md.append("**版本**: ").append(documentation.getVersion()).append("\n\n");
        md.append("**生成时间**: ").append(formatTime(documentation.getGeneratedTime())).append("\n\n");
        md.append("**接口总数**: ").append(documentation.getTotalEndpoints()).append("\n\n");
        md.append("---\n\n");
        
        for (ApiEndpoint endpoint : documentation.getEndpoints()) {
            md.append("## ").append(endpoint.getMethodDescription()).append("\n\n");
            md.append("- **接口路径**: `").append(endpoint.getHttpMethod()).append(" ").append(endpoint.getPath()).append("`").append("\n");
            md.append("- **所属类**: ").append(endpoint.getClassName()).append("\n");
            md.append("- **方法名**: ").append(endpoint.getMethodName()).append("\n\n");
            
            if (!endpoint.getParameters().isEmpty()) {
                md.append("### 请求参数\n\n");
                md.append("| 参数名 | 类型 | 位置 | 必填 | 说明 |\n");
                md.append("|--------|------|------|------|------|\n");
                
                for (ApiEndpoint.Parameter param : endpoint.getParameters()) {
                    md.append("| ").append(param.getName())
                      .append(" | ").append(param.getType())
                      .append(" | ").append(param.getLocation())
                      .append(" | ").append(param.isRequired() ? "是" : "否")
                      .append(" | ").append(param.getDescription())
                      .append(" |\n");
                }
                md.append("\n");
            }
            
            if (endpoint.getResponseBodyExample() != null && !endpoint.getResponseBodyExample().isEmpty()) {
                md.append("### 响应示例\n\n");
                md.append("```json\n");
                md.append(endpoint.getResponseBodyExample()).append("\n");
                md.append("```\n\n");
            }
            
            md.append("---\n\n");
        }
        
        File mdFile = new File(outputDir.toFile(), "API_DOCUMENTATION_" + timestamp + ".md");
        try (FileWriter writer = new FileWriter(mdFile)) {
            writer.write(md.toString());
        }
        
        File latestMd = new File(outputDir.toFile(), "README.md");
        try (FileWriter writer = new FileWriter(latestMd)) {
            writer.write(md.toString());
        }
    }
    
    private void saveAsJson(ApiDocumentation documentation, Path outputDir, String timestamp) throws IOException {
        File jsonFile = new File(outputDir.toFile(), "api-docs-" + timestamp + ".json");
        objectMapper.writeValue(jsonFile, documentation);
        
        File latestJson = new File(outputDir.toFile(), "api-docs-latest.json");
        objectMapper.writeValue(latestJson, documentation);
    }
    
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
}
