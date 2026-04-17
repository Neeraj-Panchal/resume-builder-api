package com.neeraj.resumebuilderapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    // Render mein SPRING_MAIL_PASSWORD ke andar Brevo ki API Key (xkeysib-...) dalna
    @Value("${spring.mail.password}")
    private String apiKey;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    // ==========================================
    // 1. Send HTML Email (For Verification)
    // ==========================================
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("Inside EmailService sendHtmlEmail (HTTP API): to={}, subject={}", to, subject);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail, "name", "CVPie"));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("✅ HTML Email sent successfully via Brevo API!");
        } catch (Exception e) {
            log.error("💥 Brevo API ERROR: ", e);
            // AuthService ko batane ke liye exception throw karna zaroori hai
            throw new RuntimeException("Email API failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. Send Simple Text Email (For Contact Us)
    // ==========================================
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            // Text ko HTML paragraph mein wrap karke bhej diya
            sendHtmlEmail(to, subject, "<p>" + text + "</p>");
            log.info("Contact us mail sent successfully to : " + to);
        } catch (Exception e) {
            log.error("Contact us mail sending failed to : " + to);
            System.out.println(e.getMessage());
        }
    }

    // ==========================================
    // 3. Send Email With Attachment (For Sending Resumes)
    // ==========================================
    public void sendEmailWithAttachment(String to, String subject, String bodyContent, byte[] attachment, String filename) {
        log.info("Inside EmailService sendEmailWithAttachment (HTTP API): to={}, subject={}", to, subject);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail, "name", "CVPie"));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", "<p>" + bodyContent + "</p>");

        // API ke liye PDF attachment ko Base64 String mein convert karna padta hai
        String base64Content = Base64.getEncoder().encodeToString(attachment);
        body.put("attachment", List.of(Map.of(
                "content", base64Content,
                "name", filename
        )));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("✅ Email with attachment sent successfully via Brevo API!");
        } catch (Exception e) {
            log.error("💥 Brevo API Attachment ERROR: ", e);
            throw new RuntimeException("Email API failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. Send Password Reset OTP
    // ==========================================
    public void sendPasswordResetEmail(String to, String otp) {
        log.info("Inside EmailService sendPasswordResetEmail: to={}", to);
        String subject = "CVPie - Password Reset OTP";
        String htmlContent = "<div style='font-family:sans-serif'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Your OTP to reset your password is: <strong style='font-size: 24px; color: #5b45ff;'>" + otp + "</strong></p>" +
                "<p>This OTP is valid for 10 minutes only. Please do not share it with anyone.</p>" +
                "</div>";

        // Tumhara existing function call kar rahe hain
        sendHtmlEmail(to, subject, htmlContent);
    }
}