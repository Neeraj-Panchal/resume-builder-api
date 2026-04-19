package com.neeraj.resumebuilderapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AtsService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts text from the uploaded PDF file.
     */
    public String extractTextFromPdf(MultipartFile file) {
        log.info("Starting text extraction from PDF: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new RuntimeException("Cannot process encrypted PDF files.");
            }

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("No readable text found in the PDF. Please upload a text-based PDF.");
            }

            return text;

        } catch (IOException e) {
            log.error("Error reading PDF file", e);
            throw new RuntimeException("Failed to read the PDF file.");
        }
    }

    /**
     * Analyzes the resume text against the job description using Gemini AI.
     */
    public Map<String, Object> analyzeWithGemini(String resumeText, String jobDescription) {
        log.info("Calling Gemini API for ATS Analysis...");

        String prompt = "You are an expert ATS (Applicant Tracking System) scanner. " +
                "Analyze the following Resume Text against the Job Description. " +
                "Provide a strict ATS analysis and return the result ONLY in the following JSON format. " +
                "Do not add any markdown formatting like ```json or other text outside the JSON block.\n\n" +
                "{\n" +
                "  \"score\": <number from 0 to 100>,\n" +
                "  \"matchedKeywords\": [\"keyword1\", \"keyword2\"],\n" +
                "  \"missingKeywords\": [\"keyword3\", \"keyword4\"],\n" +
                "  \"readabilityScore\": <number from 0 to 100>,\n" +
                "  \"tips\": [\"tip1\", \"tip2\", \"tip3\"]\n" +
                "}\n\n" +
                "Resume Text:\n" + resumeText + "\n\n" +
                "Job Description:\n" + jobDescription;

        // Construct Gemini Request Body
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GEMINI_API_URL + geminiApiKey,
                    entity,
                    String.class
            );

            // Parse the response to extract the JSON text returned by Gemini
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Sometime AI wraps json in markdown tags, we need to clean it
            String cleanJson = aiResponseText.replaceAll("```json", "").replaceAll("```", "").trim();

            // Convert the cleaned JSON string back to a Map and return
            return objectMapper.readValue(cleanJson, Map.class);

        } catch (Exception e) {
            log.error("Failed to analyze resume with Gemini", e);
            throw new RuntimeException("AI Analysis failed. Please try again later.");
        }
    }
}