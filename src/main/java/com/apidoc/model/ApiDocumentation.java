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
public class ApiDocumentation {
    
    private String projectName;
    
    private String version;
    
    private long generatedTime;
    
    private List<ApiEndpoint> endpoints = new ArrayList<>();
    
    private int totalEndpoints;
    
    private int coveredEndpoints;
    
    private double coverageRate;
}
