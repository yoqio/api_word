package com.apidoc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpoint {
    
    private String className;
    
    private String classDescription;
    
    private String methodName;
    
    private String methodDescription;
    
    private String httpMethod;
    
    private String path;
    
    private List<Parameter> parameters = new ArrayList<>();
    
    private ResponseInfo responseInfo;
    
    private List<ErrorCode> errorCodes = new ArrayList<>();
    
    private String requestBodyExample;
    
    private String responseBodyExample;
    
    private List<String> tags = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private String type;
        private String location;
        private boolean required;
        private String description;
        private String example;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseInfo {
        private String type;
        private String description;
        private String example;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorCode {
        private String code;
        private String message;
        private String description;
    }
}
