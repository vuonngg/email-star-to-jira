package com.example.emailtojira.service.service;

import com.example.emailtojira.model.EmailDetail;
import com.example.emailtojira.model.JiraCreateTask;

public interface JiraService {
    String createIssueFromEmail(EmailDetail emailDetail);
}
