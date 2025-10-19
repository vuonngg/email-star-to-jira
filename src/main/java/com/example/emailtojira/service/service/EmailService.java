package com.example.emailtojira.service.service;

import com.example.emailtojira.model.EmailDetail;

import java.util.List;

public interface EmailService {
    String getValidAccessToken();

    List<EmailDetail> getStarEmails();

    void unstarEmail(String emailId);
}
