package com.neeraj.resumebuilderapi.controller;

import com.neeraj.resumebuilderapi.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping(value = "/send-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendResumeByEmail(
            @RequestParam("recipientEmail") String recipientEmail,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "message", required = false) String message,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) {

        Map<String, Object> response = new HashMap<>();

        try {
            // step 1 : validate the inputs
            if(recipientEmail == null || pdfFile == null || pdfFile.isEmpty()){
                response.put("success", false);
                response.put("message", "Missing required fields or empty file");
                return ResponseEntity.badRequest().body(response);
            }

            // step 2 : get the file data
            byte[] pdfBytes = pdfFile.getBytes();
            String originalFilename = pdfFile.getOriginalFilename();
            String filename = (originalFilename != null && !originalFilename.isEmpty()) ? originalFilename : "resume.pdf";

            // step 3 : Prepare the email Content
            String emailSubject = (subject != null && !subject.isEmpty()) ? subject : "Resume Application";
            String emailBody = (message != null && !message.isEmpty()) ? message : "Please find my resume attached.\n\nBest regards";

            log.info("Attempting to send email to: {}", recipientEmail);

            // step 4 : call the service method
            emailService.sendEmailWithAttachment(recipientEmail, emailSubject, emailBody, pdfBytes, filename);

            // step 5 : return the response
            log.info("Email sent successfully!");
            response.put("success", true);
            response.put("message", "Resume sent successfully to " + recipientEmail);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // YAHAN PAR ASLI ERROR PRINT HOGA!
            log.error("💥 ERROR WHILE SENDING EMAIL: ", e);
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Failed to send email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}