package com.example.emailtojira.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.gmail.Gmail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfig {

    // --- Cấu hình JIRA ---
    private String jiraBaseUrl;
    private String jiraUsername;
    private String jiraToken;
    private String jiraProjectKey;

    // Cấu hình Email API ) ---
    private String emailClientId;
    private String emailClientSecret;

    private String emailRefreshToken;  // ĐỂ LẤY TOKEN MỚI KHI HẾT HIỆU LỰC
    private String emailAccessToken;   // MÃ NGẮN HẠN
    private long accessTokenExpiryTime = 0; // THỜI ĐIỂM HẾT HẠN CỦA TOKEN

}