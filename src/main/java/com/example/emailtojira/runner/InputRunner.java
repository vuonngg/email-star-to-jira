package com.example.emailtojira.runner;

import com.example.emailtojira.config.AppConfig;
import com.example.emailtojira.service.AutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class InputRunner implements CommandLineRunner {
    private final AppConfig config;
    private final AutomationService automationService;
    private final Scanner scanner = new Scanner(System.in);



    @Override
    public void run(String... args) throws Exception {

        System.out.println("\n-------------------------------------------");
        System.out.println("\n--- THÔNG TIN JIRA ---");

        // 1. JIRA BASE URL
        System.out.print("1. JIRA BASE URL: ");
        config.setJiraBaseUrl(scanner.nextLine().trim());

        // 2. JIRA USERNAME
        System.out.print("2. JIRA USERNAME (Email): ");
        config.setJiraUsername(scanner.nextLine().trim());

        // 3. JIRA API project
        System.out.print("3. JIRA PROJECT KEY (Ví dụ: PROJ): ");
        config.setJiraProjectKey(scanner.nextLine().trim());


        // 4. JIRA PROJECT KEY
        System.out.print("4. JIRA API TOKEN: ");
        config.setJiraToken(scanner.nextLine().trim());



        System.out.println("\n--- THÔNG TIN EMAIL ---");

        // 5. EMAIL API CLIENT ID
        System.out.print("5. EMAIL API CLIENT ID: ");
        config.setEmailClientId(scanner.nextLine().trim());

        // 6. EMAIL API CLIENT SECRET
        System.out.print("6. EMAIL API CLIENT SECRET: ");
        config.setEmailClientSecret(scanner.nextLine().trim());

        System.out.print("7. EMAIL REFRESH TOKEN: ");
        config.setEmailRefreshToken(scanner.nextLine().trim());

        System.out.println("-------------------------------------------\n");
        // Tạo luồng mới dùng do wile để auto
        new Thread(() -> {
            try {
                boolean firstRun = true;

                do {
                    if (!firstRun) {
                        Thread.sleep(5000);
                    }

                 // gọi hàm chạy lần đầu
                    automationService.startConvertEmailToJira();

                    firstRun = false;

                } while (true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Lỗi luồng lặp lại: " + e.getMessage());
            }
        }).start();
    }
}
