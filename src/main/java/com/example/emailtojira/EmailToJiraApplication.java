package com.example.emailtojira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmailToJiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailToJiraApplication.class, args);
    }

}
