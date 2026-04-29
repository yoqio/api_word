package com.apidoc.notification;

import com.apidoc.config.ApiDocConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    
    private final ApiDocConfig config;
    private final JavaMailSender mailSender;
    
    public NotificationService(ApiDocConfig config, JavaMailSender mailSender) {
        this.config = config;
        this.mailSender = mailSender;
    }
    
    public void sendNotification(String subject, String content) {
        if (!config.getNotification().isEmailEnabled()) {
            log.info("邮件通知未启用");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(config.getNotification().getFrom());
            message.setTo(config.getNotification().getTo());
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            
            log.info("通知邮件已发送: {}", subject);
        } catch (Exception e) {
            log.error("发送通知邮件失败", e);
        }
    }
    
    public void sendDocumentUpdateNotification(int endpointCount) {
        String subject = "【API文档更新通知】";
        String content = String.format(
            "API接口文档已自动更新。\n\n" +
            "更新时间: %s\n" +
            "接口总数: %d\n\n" +
            "请查看最新文档。",
            java.time.LocalDateTime.now().toString(),
            endpointCount
        );
        
        sendNotification(subject, content);
    }
}
