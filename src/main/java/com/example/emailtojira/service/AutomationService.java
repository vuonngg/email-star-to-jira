package com.example.emailtojira.service;

import com.example.emailtojira.config.AppConfig;
import com.example.emailtojira.model.EmailDetail;
import com.example.emailtojira.service.service.EmailService;
import com.example.emailtojira.service.service.JiraService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutomationService {

    // Giữ nguyên AppConfig (Mặc dù không dùng trực tiếp ở đây, nó hữu ích cho việc log hoặc cấu hình)
    private final AppConfig appConfig;
    private final EmailService emailService;
    private final JiraService jiraService; // 👈 THÊM DEPENDENCY NÀY

    // Tùy chọn: Dùng @Scheduled để chạy hàm này tự động theo lịch
    // @Scheduled(fixedDelay = 600000) // Ví dụ: Chạy mỗi 10 phút (600,000 ms)
    public void startConvertEmailToJira(){

        System.out.println("--- BẮT ĐẦU CHU TRÌNH TỰ ĐỘNG ---");

        // 1. Lấy Access Token hợp lệ (sẽ refresh nếu cần)
        String validAccessToken = emailService.getValidAccessToken();
        System.out.println("--- AccessToken đã được cập nhật/kiểm tra. ---");

        // 2. Lấy danh sách email đã gắn sao
        List<EmailDetail> starredEmails = emailService.getStarEmails();
        System.out.println("--- Tìm thấy " + starredEmails.size() + " email cần xử lý.");

        if (starredEmails.isEmpty()) {
            System.out.println("--- Không có email nào để tạo Jira Issue.");
        }

        // 3. DUYỆT VÀ XỬ LÝ TỪNG EMAIL
        for (EmailDetail email : starredEmails) {
            try {
                // a. TẠO JIRA ISSUE (Sử dụng JiraService)
                String issueKey = jiraService.createIssueFromEmail(email);
                System.out.println("-> [THÀNH CÔNG] Email ID " + email.getId() + " đã tạo Jira Issue: " + issueKey);

                // b. XÓA ĐÁNH DẤU SAO (Chỉ khi tạo Issue thành công)
                emailService.unstarEmail(email.getId());

            } catch (Exception e) {
                // c. XỬ LÝ LỖI
                // Nếu lỗi, email vẫn giữ sao để xử lý lại lần sau
                System.err.println("!!! [LỖI XỬ LÝ] Email ID " + email.getId() + " gặp lỗi. Giữ lại sao để thử lại.");
                System.err.println("Chi tiết lỗi: " + e.getMessage());
                // (Tùy chọn) e.printStackTrace();
            }
        }

        System.out.println("--- KẾT THÚC CHU TRÌNH TỰ ĐỘNG ---\n");
    }


}
