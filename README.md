
# 3-Day Fresher Engineering Automation Challenge

## Objective
Identify and implement a small project that helps automate, optimize, or improve your own workflow as an Engineer. The project should target something that reduces manual work, simplifies repetitive tasks, or uses AI/MCP to enhance your daily efficiency.

## Duration
3 Days

## Expectations

- Pick a problem area from your current workflow that causes friction or consumes time.
- Propose a small automation, tool, or AI-based solution to address that problem.
- Build a working prototype or script that demonstrates the solution.
- Prepare a short demo or walkthrough of what you built.
- If you are unsure or stuck on ideas, you may pick from the default challenge ideas below.

## Default Challenge Ideas (Optional)

- Email to Task Converter - Turn flagged emails into tasks automatically.
- Meeting Summary Extractor - Auto-generate action items from MS Teams or Zoom meeting transcripts.
- Daily Standup Generator - Draft your daily standup update using data from Git, Jira, or MS Teams.
- Code Review Reminder Bot - Get reminders for pending pull request reviews.
- Code Snippet Organizer with Search - Save and search your personal code snippets.
- User Story Auto-Breaker - Break large user stories into tasks based on rules.
- MS Teams to Knowledge Base Bridge - Suggest good discussions from MS Teams for the knowledge base.
- Personal Notes MCP - Manage and search personal notes using MCP.
- AI PR Reviewer - AI reviews pull requests based on your custom rules.
- Team Alerts Summarizer (MS Teams) - Summarize noisy alerts and send clean updates via MS Teams.

## Deliverable

- Working prototype (script, bot, integration, etc.)
- Short demo (video, live walkthrough, or documentation)
- Reflection on how this project helps your workflow

## Goal

Learn to take initiative, solve practical problems, and build useful workflow tools with automation and AI.

## Deadline
3 days from project start.

## Hướng dẫn
### Email-to-Jira Automation
Công cụ này tự động kiểm tra các email được gắn sao (starred emails), làm sạch nội dung HTML thô của email, và tạo các Issue mới (Loại Task) trong dự án Jira được chỉ định.
### 🚀 1. Cài đặt và Yêu cầu
#### Yêu cầu Tiên quyết
1 Java Development Kit (JDK): Phiên bản 17+ (hoặc phiên bản tương thích).

2 Apache Maven: Để quản lý dependency và build dự án.

3 Jira Cloud/Server: Với quyền tạo Issue trong dự án mục tiêu.
### ⚙️ 2. Cấu hình Ứng dụng
#### Bạn cần cấu hình thông tin xác thực cho cả dịch vụ Email và Jira.
##### Cấu hình Jira API (Sử dụng Basic Auth) : https://www.atlassian.com
``` 
  BASE URL (ví dụ: https://vuossngggg.atlassian.net)
  USERNAME : là email của bạn sử dụng đăng ký
  PROJECT KEY (Ví dụ: PROJ):
  API TOKEN: (ví dụ: ATATT3xFfGF0u_qEL2_Gdw6BH5AMVmp2z-JuTL...)
```
##### Cấu hình Dịch vụ Email (Gmail/Google OAuth2) : https://console.cloud.google.com
``` 
  CLIENT ID (ví dụ:  389680537667-iddp2ub8mrp...)
  CLIENT SECRET (ví dụ: GOCSPX-85u0A9PTd...)
  REFRESH TOKEN (Ví dụ: 1//0eBNMazMK8...):
```
### 🏃 3. Chạy Ứng dụng
#### Phương pháp Phát triển (Dùng IntelliJ IDEA) 

1. Build Dự án (Tùy chọn: Để đảm bảo Dependency)
- Sử dụng giao diện Maven trong IntelliJ IDEA để build:
- Mở tab Maven (thường nằm ở bên phải IDE).
- Mở rộng thư mục dự án 

- Nhấp đúp vào clean rồi nhấp đúp vào install.

2.  Khởi chạy Ứng dụng (Khuyến nghị)
- Mở file chứa lớp chính của ứng dụng (thường là file *Application.java có chứa hàm main).

- Nhấp vào nút mũi tên màu xanh lá cây (Run/Play) bên cạnh hàm main và chọn Run (Chạy) hoặc Debug (Gỡ lỗi).

#### Chạy nhanh qua Maven Tool Window
- Mở tab Maven (bên phải IDE).
- Tìm đến thư mục dự án > Plugins > spring-boot > Nhấp đúp vào spring-boot:run.

#### Phương pháp Tiêu chuẩn (Deployment/Sản xuất)
Đây là cách chuẩn để build và chạy ứng dụng trên môi trường server hoặc Docker (sử dụng Terminal).

- Build Dự án (Tạo file JAR):

- Mở Terminal tại thư mục gốc của dự án.

- Chạy lệnh Maven để tạo file JAR có thể chạy được:
```
mvn clean install
 ```
Khởi chạy Ứng dụng:

Chạy file JAR đã build (thường nằm trong thư mục target):

```
java -jar target/email-to-jira-automation.jar
```

#### Sau khi khởi chạy, bạn sẽ phải nhập ngay thông tin cấu hình của Jira, Email và ứng dụng sẽ áp dụng các cấu hình đã cung cấp để bắt đầu chu trình kiểm tra và tạo Issue tự động.
#### video demo: https://youtu.be/L5MkiUt2z-E