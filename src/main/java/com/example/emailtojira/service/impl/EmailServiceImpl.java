package com.example.emailtojira.service.impl;

import com.example.emailtojira.config.AppConfig;
import com.example.emailtojira.model.EmailDetail;
import com.example.emailtojira.service.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.gmail.model.ModifyMessageRequest;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service

public class EmailServiceImpl implements EmailService {
    private  Gmail gmailService;
    private static final String USER_ID = "me";
    private final  AppConfig appConfig;
    private final RestTemplate restTemplate; // BẮT BUỘC
    private final ObjectMapper objectMapper;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    @Override
    public String getValidAccessToken() {
// Khoảng đệm an toàn 5 phút (tính bằng mili-giây)
        final long SAFETY_BUFFER_MS = 5 * 60 * 1000L;

        // 1. Điều kiện để làm mới token:
        //    a. Access Token chưa bao giờ được lấy (ExpiryTime <= 0)
        //    b. Hoặc Token hiện tại đã hết/sắp hết hạn (Thời gian hiện tại đã vượt qua ExpiryTime - đệm)
        boolean tokenNeedsRefresh = (appConfig.getAccessTokenExpiryTime() <= 0 ||
                System.currentTimeMillis() >= (appConfig.getAccessTokenExpiryTime() - SAFETY_BUFFER_MS));

        if (tokenNeedsRefresh) {

            // 2. Kiểm tra Refresh Token (Điều kiện BẮT BUỘC cho việc làm mới)
            if (appConfig.getEmailRefreshToken() == null || appConfig.getEmailRefreshToken().isEmpty()) {
                throw new RuntimeException("Refresh Token không tồn tại. Vui lòng nhập RefreshToken.");
            }

            System.out.println("[TOKEN] Access Token đã hết hạn. Đang tiến hành làm mới...");

            // 3. GỌI HÀM LÀM MỚI TOKEN (refreshAccessToken phải được viết riêng)
            refreshAccessToken();

            // Sau khi refreshAccessToken() chạy xong, Access Token và ExpiryTime đã được cập nhật.
        }

        // 4. Trả về Access Token hợp lệ (vừa được làm mới hoặc còn hạn)
        return appConfig.getEmailAccessToken();
    }




    @Override
    public List<EmailDetail> getStarEmails() {
        if (gmailService == null) {
            System.err.println("[EMAIL SERVICE] Dịch vụ Gmail chưa khởi tạo.");
            return Collections.emptyList();
        }

        List<EmailDetail> emailDetails = new ArrayList<>();
        // Truy vấn tìm kiếm email đã gắn sao
        final String STARRED_QUERY = "is:starred";

        try {
            // 1. Lấy danh sách ID email thỏa mãn truy vấn
            ListMessagesResponse response = gmailService.users().messages().list(USER_ID)
                    .setQ(STARRED_QUERY)
                    .execute();

            List<Message> messages = response.getMessages();

            if (messages == null || messages.isEmpty()) {
                System.out.println("Không tìm thấy email gắn sao nào.");
                return Collections.emptyList();
            }

            System.out.println("Tìm thấy " + messages.size() + " email gắn sao cần xử lý.");

            // 2. Lấy nội dung chi tiết cho từng ID
            for (Message message : messages) {
                Message fullEmail = getEmailDetails(message.getId());
                if (fullEmail != null) {

                    // 3. Trích xuất dữ liệu cần thiết
                    EmailDetail detail = extractEmailData(fullEmail);
                    emailDetails.add(detail);

                    // 4. (Tùy chọn) Đánh dấu email là ĐÃ XỬ LÝ (ví dụ: xóa sao)
                    // markEmailAsProcessed(message.getId());
                }
            }

        } catch (IOException e) {
            System.err.println("[EMAIL SERVICE] Lỗi khi lấy email gắn sao: " + e.getMessage());
            e.printStackTrace();
        }

        return emailDetails;
    }

    @Override
    public void unstarEmail(String emailId) {
        if (gmailService == null) {
            System.err.println("[EMAIL SERVICE] Dịch vụ Gmail chưa khởi tạo. Không thể xóa sao email " + emailId);
            return;
        }

        try {
            // TẠO ĐỐI TƯỢNG MODIFYMESSAGEREQUEST để chỉ rõ yêu cầu sửa đổi nhãn.
            // Đây là cách tiếp cận chính xác và mạnh mẽ hơn.
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Collections.singletonList("STARRED"));

            // Gửi yêu cầu modify(), sử dụng đối tượng modifyRequest
            // LƯU Ý: Phương thức modify() trong API Client phiên bản mới chấp nhận Message hoặc ModifyMessageRequest.
            // Tuy nhiên, để sửa đổi nhãn, ta cần truyền đối tượng chứa sự sửa đổi đó.
            gmailService.users().messages().modify(USER_ID, emailId, modifyRequest).execute();

            System.out.println("[EMAIL SERVICE] Đã xóa đánh dấu sao khỏi Email ID: " + emailId);

        } catch (IOException e) {
            System.err.println("[EMAIL SERVICE] Lỗi khi xóa đánh dấu sao email " + emailId + ": " + e.getMessage());
        }
    }
    //gán token lấy ra mới vào gmail sẻvice của google
    private void initializeGmailService(String accessToken) {
        try {
            GoogleCredential credential = new GoogleCredential()
                    .setAccessToken(accessToken); // Sử dụng Access Token hợp lệ

            this.gmailService = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), // Hoặc JacksonFactory.getDefaultInstance()
                    credential)
                    .setApplicationName("EmailToJira-Automation")
                    .build();

            System.out.println("[EMAIL SERVICE] Gmail Service đã khởi tạo thành công.");
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("[EMAIL SERVICE] Lỗi khởi tạo Gmail Service: " + e.getMessage());
        }
    }


    //// lấy token mới
    private void refreshAccessToken() {
        // Kiểm tra Refresh Token (chỉ để đảm bảo an toàn)
        if (appConfig.getEmailRefreshToken() == null || appConfig.getEmailRefreshToken().isEmpty()) {
            throw new RuntimeException("Refresh Token không tồn tại. Không thể làm mới token.");
        }
        // 1. Cấu hình Headers: Gửi dữ liệu dưới dạng x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 2. Cấu hình Body: Chứa 4 tham số bắt buộc
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", appConfig.getEmailClientId()); // SỬ DỤNG appConfig
        body.add("client_secret", appConfig.getEmailClientSecret()); // SỬ DỤNG appConfig
        body.add("refresh_token", appConfig.getEmailRefreshToken()); // KHÓA DÀI HẠN
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // 3. Gửi yêu cầu POST API TOKEN EXCHANGE
            ResponseEntity<String> response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST, request, String.class);

            // 4. Phân tích phản hồi JSON
            Map<String, Object> tokenMap = objectMapper.readValue(response.getBody(), Map.class);

            // Lấy Access Token mới và thời gian sống (giây)
            String newAccessToken = (String) tokenMap.get("access_token");
            int expiresInSeconds = (Integer) tokenMap.get("expires_in");

            // 5. CẬP NHẬT CẤU HÌNH
            appConfig.setEmailAccessToken(newAccessToken);
            initializeGmailService(newAccessToken);

            // Tính toán thời điểm hết hạn mới
            long newExpiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000L);
            appConfig.setAccessTokenExpiryTime(newExpiryTime);

            System.out.println("[TOKEN] Access Token làm mới thành công. Hết hạn lúc: " + new java.util.Date(newExpiryTime));

        } catch (Exception e) {
            System.err.println("[TOKEN ERROR] Lỗi khi làm mới Access Token: " + e.getMessage());

            throw new RuntimeException("Không thể làm mới Access Token. Vui lòng kiểm tra Refresh Token và Client Keys.", e);
        }
    }


    public Message getEmailDetails(String messageId) throws IOException {
        if (gmailService == null) return null;
        // Dùng format FULL để lấy body và header đầy đủ
        return gmailService.users().messages().get(USER_ID, messageId).setFormat("full").execute();
    }

    private EmailDetail extractEmailData(Message message) {
        EmailDetail detail = new EmailDetail();
        detail.setId(message.getId());

        // Trích xuất Header
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            // Cần có import com.google.api.services.gmail.model.MessagePartHeader;
            for (com.google.api.services.gmail.model.MessagePartHeader header : message.getPayload().getHeaders()) {
                if (header.getName().equals("Subject")) {
                    detail.setTieuDe(header.getValue());
                } else if (header.getName().equals("From")) {
                    detail.setNguoiGui(header.getValue());
                }
            }
        }

        // Trích xuất Body
        String emailBody = extractBodyFromPayload(message.getPayload());
        detail.setNoiDung(emailBody);

        return detail;
    }

    private String extractBodyFromPayload(MessagePart payload) {
        if (payload == null) return "";

        String mimeType = payload.getMimeType();

        // 1. Nếu là TEXT/PLAIN hoặc TEXT/HTML, LẤY NỘI DUNG
        if (mimeType != null && (mimeType.equals("text/plain") || mimeType.equals("text/html"))) {
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                byte[] bodyBytes = Base64.getUrlDecoder().decode(payload.getBody().getData());
                return new String(bodyBytes, StandardCharsets.UTF_8);
            }
        }

        // 2. Nếu là MULTIPART hoặc có PARTS
        if (payload.getParts() != null) {
            // TÁCH VÒNG LẶP: Ưu tiên tìm text/plain
            for (MessagePart part : payload.getParts()) {
                if (part.getMimeType().equals("text/plain")) {
                    String result = extractBodyFromPayload(part); // Gọi đệ quy
                    if (!result.isEmpty()) return result;
                }
            }

            // VÒNG LẶP THỨ HAI: Nếu không có text/plain, thử tìm text/html
            for (MessagePart part : payload.getParts()) {
                if (part.getMimeType().equals("text/html")) {
                    String result = extractBodyFromPayload(part);
                    if (!result.isEmpty()) return result;
                }
            }

            // VÒNG LẶP THỨ BA : Nếu vẫn không tìm thấy, duyệt qua các phần tử phức tạp
            // (multipart/alternative, multipart/mixed, v.v.)
            for (MessagePart part : payload.getParts()) {
                if (part.getParts() != null) {
                    String result = extractBodyFromPayload(part); // Gọi đệ quy sâu hơn
                    if (!result.isEmpty()) return result;
                }
            }
        }

        return "";
    }


}
