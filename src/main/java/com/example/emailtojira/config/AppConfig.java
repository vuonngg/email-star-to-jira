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

    // --- Cấu hình Email API (Ví dụ: OAuth 2.0) ---
    private String emailClientId;
    private String emailClientSecret;
    // (Trong thực tế, bạn sẽ cần cả Refresh Token/Access Token, nhưng ta dùng Client ID/Secret cho mục đích demo cấu hình)
    private String emailRefreshToken;  // MÃ DÀI HẠN: Lấy sau khi trao đổi Authorization Code lần đầu
    private String emailAccessToken;   // MÃ NGẮN HẠN: Được làm mới liên tục
    private long accessTokenExpiryTime = 0; // THỜI ĐIỂM HẾT HẠN: Dùng để kiểm tra xem có cần làm mới không

}