package com.apidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "apidoc")
public class ApiDocConfig {
    
    private String scanPackage = "com.example.controller";
    
    private String outputDir = "./api-docs";
    
    private String docFormat = "markdown";
    
    private LlmConfig llm = new LlmConfig();
    
    private GitConfig git = new GitConfig();
    
    private NotificationConfig notification = new NotificationConfig();
    
    @Data
    public static class LlmConfig {
        private String apiUrl = "https://api.openai.com/v1/chat/completions";
        private String apiKey = "";
        private String model = "gpt-3.5-turbo";
        private double temperature = 0.7;
        private int maxTokens = 2000;
    }
    
    @Data
    public static class GitConfig {
        private boolean enabled = true;
        private String repoPath = ".";
        private String branch = "main";
    }
    
    @Data
    public static class NotificationConfig {
        private boolean emailEnabled = false;
        private String smtpHost;
        private int smtpPort = 587;
        private String username;
        private String password;
        private String from;
        private String[] to;
    }
}
