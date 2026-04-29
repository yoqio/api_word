package com.apidoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiDocApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiDocApplication.class, args);
    }
}
