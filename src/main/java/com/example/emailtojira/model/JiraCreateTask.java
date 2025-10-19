package com.example.emailtojira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraCreateTask {


    private Fields fields; // Đối tượng chứa tất cả các trường dữ liệu Task

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Fields {

        // 1. Tiêu đề Task (từ email subject)
        private String summary;

        // 2. Mô tả Task (từ email body)
        private String description;

        // 3. Dự án mà Task thuộc về (ví dụ: PROJECT_KEY)
        private Project project;

        // 4. Loại Task (ví dụ: Task, Bug, Story)
        private IssueType issuetype;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Project {
        private String key;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueType {
        private String name;
    }

}
