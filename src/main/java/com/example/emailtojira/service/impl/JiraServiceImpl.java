package com.example.emailtojira.service.impl;

import com.example.emailtojira.config.AppConfig;
import com.example.emailtojira.model.EmailDetail;
import com.example.emailtojira.model.JiraCreateTask;
import com.example.emailtojira.service.service.JiraService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Service // Đảm bảo Spring quản lý lớp này
@RequiredArgsConstructor // Tạo constructor cho các trường final
public class JiraServiceImpl implements JiraService {

    private final AppConfig appConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Dùng để chuyển đổi Java Object sang JSON

    private static final String CREATE_ISSUE_URL = "/rest/api/2/issue";

    @Override
    public String createIssueFromEmail(EmailDetail detail) {

        // Xây dựng URL đầy đủ (đảm bảo xử lý dấu gạch chéo)
        String jiraUrl = appConfig.getJiraBaseUrl().endsWith("/")
                ? appConfig.getJiraBaseUrl() + CREATE_ISSUE_URL.substring(1)
                : appConfig.getJiraBaseUrl() + CREATE_ISSUE_URL;

        // 1. Tạo Headers (Xác thực Basic Auth)
        HttpHeaders headers = createAuthHeaders();

        // 2. Chuyển đổi EmailDetail sang mô hình JiraCreateTask
        JiraCreateTask jiraTask = buildJiraTaskFromEmail(detail);

        // 3. Thực thi Request
        try {
            // Chuyển đổi đối tượng Java thành JSON String
            String jsonBody = objectMapper.writeValueAsString(jiraTask);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            System.out.println("[JIRA SERVICE] Đang gửi Issue: " + detail.getTieuDe());

            // Gửi yêu cầu POST và nhận phản hồi
            ResponseEntity<Map> response = restTemplate.exchange(
                    jiraUrl, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Phản hồi thành công chứa khóa Issue (ví dụ: "IT-123")
                String issueKey = (String) response.getBody().get("key");
                System.out.println("[JIRA SERVICE] Tạo Issue thành công. KEY: " + issueKey);
                return issueKey;
            } else {
                // Xử lý lỗi nếu Jira trả về lỗi 4xx/5xx
                throw new RuntimeException("Jira API lỗi. Status: " + response.getStatusCode() + ". Body: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("[JIRA SERVICE] Lỗi không thể tạo Issue từ email " + detail.getId() + ": " + e.getMessage());
            // Ném lại ngoại lệ để AutomationService bắt và xử lý việc giữ lại sao
            throw new RuntimeException("Lỗi tạo Jira Issue.", e);
        }
    }

    /**
     * Tạo Headers chứa Basic Authentication (Username:Token).
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Mã hóa Email (Username) và API Token Jira sang Base64
        String auth = appConfig.getJiraUsername() + ":" + appConfig.getJiraToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Chuyển đổi dữ liệu từ EmailDetail sang mô hình JiraCreateTask.
     */
    private JiraCreateTask buildJiraTaskFromEmail(EmailDetail detail) {
        // 1. Tạo Project
        JiraCreateTask.Project project = new JiraCreateTask.Project(
                appConfig.getJiraProjectKey()
        );

        // 2. Tạo Issue Type (Dùng "Task" mặc định)
        // LƯU Ý: Phải khớp với tên loại Issue trong Jira của bạn
        JiraCreateTask.IssueType issueType = new JiraCreateTask.IssueType("Task");

        // 3. Tạo Fields
        String summary = "Email: " + detail.getTieuDe();
        String cleanedBody = cleanAndFormatHtml(detail.getNoiDung()); // Gọi hàm lọc

        // Description: Nội dung email được định dạng
        String description =
                cleanedBody + "\n" +
                "----------------------------------------\n" +
                "Người gửi: " + detail.getNguoiGui();

        JiraCreateTask.Fields fields = new JiraCreateTask.Fields(
                summary,
                description,
                project,
                issueType
        );
        // 4. Tạo đối tượng Task hoàn chỉnh
        return new JiraCreateTask(fields);
    }

    private String cleanAndFormatHtml(String rawHtml) {
        if (rawHtml == null || rawHtml.isEmpty()) {
            return "Nội dung email trống.";
        }
        // 1. LỌC HTML VÀ CHUYỂN ENTITY: Jsoup không chỉ loại bỏ thẻ mà còn chuyển đổi
        String cleanText = org.jsoup.Jsoup.parse(rawHtml).text();
        // 2. LÀM SẠCH KÝ TỰ ĐIỀU KHIỂN & KHOẢNG TRẮNG DƯ THỪA
        // a. Loại bỏ các ký tự \r, \t, và nhiều khoảng trắng liên tiếp, giữ lại \n
        String cleanedText = cleanText.replaceAll("[\r\t]+", " ").replaceAll(" +", " ");
        // 3. ĐỊNH DẠNG: Thêm ngắt đoạn để mô tả Jira dễ đọc
        // a. Ngắt đoạn sau các dấu chấm câu (Rất quan trọng)
        String formattedText = cleanedText.replace(". ", ".\n\n");
        formattedText = formattedText.replace("! ", "!\n\n");
        formattedText = formattedText.replace("? ", "?\n\n");
        // b. Thay thế các nhóm khoảng trắng lớn (nếu còn sót) thành 2 lần xuống dòng
        formattedText = formattedText.replaceAll(" {3,}", "\n\n");
        // c. Loại bỏ khoảng trắng ở đầu và cuối chuỗi
        formattedText = formattedText.trim();
        return formattedText;
    }
}
