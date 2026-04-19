package com.neeraj.resumebuilderapi.controller;

import com.neeraj.resumebuilderapi.service.AtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ats")
@RequiredArgsConstructor
@Slf4j
public class AtsController {

    private final AtsService atsService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription) {

        log.info("Received ATS analysis request for file: {}", file.getOriginalFilename());

        try {
            // Step 1: PDF se text nikalo
            String resumeText = atsService.extractTextFromPdf(file);

            // Step 2: Extract kiye text ko Gemini AI ke paas bhejo analysis ke liye
            Map<String, Object> atsReport = atsService.analyzeWithGemini(resumeText, jobDescription);

            // Step 3: Success aur Report return karo
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Analysis completed successfully",
                    "data", atsReport
            ));

        } catch (Exception e) {
            log.error("Error in ATS analysis", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}