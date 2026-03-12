package com.neeraj.resumebuilderapi.controller;

import com.neeraj.resumebuilderapi.document.Contact;
import com.neeraj.resumebuilderapi.service.ContactService;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitContactForm(@RequestBody Contact contact) {
        try {
            Contact savedMessage = contactService.saveMessageAndSendEmail(contact);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message sent and saved successfully!",
                    "data", savedMessage
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send message: " + e.getMessage()
            ));
        }
    }

}
