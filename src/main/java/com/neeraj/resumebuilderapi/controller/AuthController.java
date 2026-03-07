package com.neeraj.resumebuilderapi.controller;

import com.neeraj.resumebuilderapi.document.User;
import com.neeraj.resumebuilderapi.dto.AuthResponse;
import com.neeraj.resumebuilderapi.dto.LoginRequest;
import com.neeraj.resumebuilderapi.dto.RegisterRequest;
import com.neeraj.resumebuilderapi.service.AuthService;
import com.neeraj.resumebuilderapi.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.neeraj.resumebuilderapi.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {


    private final AuthService authService;

    private final FileUploadService fileUploadService;

    //@Valid annotation is a security guard that checks the conditions i have written in the dto class

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
            log.info("Inside AuthController - register(): {}",request);
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token){
        log.info("Inside AuthController - verifyEmail(): {}",token);

        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Email verified successfully"));
    }

    @PostMapping(UPLOAD_PROFILE_IMAGE)
    public ResponseEntity<?> uploadImage(@RequestPart("image")MultipartFile file) throws IOException {
        log.info("Inside AuthController - uploadImage()");
        Map<String, String> response = fileUploadService.uploadSingleImage(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        log.info("Inside AuthController - login(): {}",request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body){
        //step 1 : get the email from request
        String email = body.get("email");

        //step 2 : Add the validations
        if(Objects.isNull(email)){
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        //step 3 : call the service method to resend the verification email
        authService.resendVerification(email);

        //step 4 : return response
        return ResponseEntity.ok(Map.of("success", true, "message", "Verfication email send successfully"));

    }

    @GetMapping(PROFILE)
    public ResponseEntity<?> getProfile(Authentication authentication){
        //step 1 : get the principal object
        Object principalObject = authentication.getPrincipal();

        //step 2 : call the service method
        AuthResponse currentProfile = authService.getProfile(principalObject);

        //step 3 : return the response
        return ResponseEntity.ok(currentProfile);
    }


}
