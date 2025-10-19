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

        private String summary;

        private String description;

        private Project project;

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
