package com.neeraj.resumebuilderapi.service;

import com.neeraj.resumebuilderapi.document.User;
import com.neeraj.resumebuilderapi.dto.AuthResponse;
import com.neeraj.resumebuilderapi.dto.LoginRequest;
import com.neeraj.resumebuilderapi.dto.RegisterRequest;
import com.neeraj.resumebuilderapi.exception.ResourceExistsException;
import com.neeraj.resumebuilderapi.repository.UserRepository;
import com.neeraj.resumebuilderapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request){
        log.info("Inside AuthService : register() {}",request);

        //checking the user is already exists with the email or not
        if(userRepository.existsByEmail((request.getEmail()))){
            throw new ResourceExistsException("User already exists with this email");
        }

        //Only the memory is created for the new user
        User newUser = toDocument(request);

        //User is saved in Mongodb
        User savedUser = userRepository.save(newUser);

        //send verification email

        sendVerificationEmail(newUser);


        //returning the user object to frontend (Client) which takes the response and give the feedback of user registered successfully or redirect the user to the login page
        return toResponse(savedUser);
    }

    private void sendVerificationEmail(User newUser) {
    log.info("Inside AuthService : sendVerificationEmail(): {}",newUser);
        try{
            String link = appBaseUrl+"/api/auth/verify-email?token="+newUser.getVerificationToken();
            String html = "<div style='font-family:sans-serif'>"+
                    "<h2>Verify your email</h2>"+
                    "<p>Hi "+newUser.getName() + ", please confirm your email to activate your account. </p>"+
                    "<p><a href='" + link + "' style = 'display:inline-block;padding:10px 16px;background:#6366f1;color:#fff;border-radius:6px;text-decoration:none'>Verify Email</a></p>"
                    + "<p>Or copy this link: "+ link + "</p>"
                    + "<p>This link expires in 24 hours. </p> "
                    + "</div>";

            emailService.sendHtmlEmail(newUser.getEmail(), "Verify your email",html);

        } catch (Exception e){
            log.error("💥 MAIL SEND FAILED but user is saved: {}",e.getMessage());
//            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    //method used to give the response for the user registration
    private AuthResponse toResponse(User savedUser){
        return AuthResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .profileImageUrl(savedUser.getProfileImageUrl())
                .subscriptionPlan(savedUser.getSubscriptionPlan())
                .emailVerified(savedUser.isEmailVerified())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    //method used to create new user
    private User toDocument(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void verifyEmail(String token){
        log.info("Inside AuthService : verifyEmail(): {}",token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        //if token is present and the expiration time is of before now's time so throw the exception
        if(user.getVerificationToken() != null && user.getVerificationExpires().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Verification token has expired. Please request new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);

    }


    public AuthResponse login(LoginRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Email or password"));

        if(!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())){
            throw new UsernameNotFoundException("Invalid Email or password");
        }

        if(!existingUser.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        String token = jwtUtil.generateJwtToken(existingUser.getId());

        AuthResponse response = toResponse(existingUser);
        response.setToken(token);
        return response;
    }

    //USED TO RESEND THE VERIFICATION EMAIL
    public void resendVerification(String email) {
        //step 1 : Fetch the user account using the email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //step 2 : check the email is verified
        if(user.isEmailVerified()){
            throw new RuntimeException("Email is already verified.");
        }

        //step 3 : set the new verification token and expiration time
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));

        //step 4 : Update the user
        userRepository.save(user);

        //step 5 : resend the verification email
        sendVerificationEmail(user);
    }

    public AuthResponse getProfile(Object principalObject) {
        User existingUser = (User)principalObject;
        return toResponse(existingUser);
    }

    // Generate OTP and Send Mail
    public void forgotPassword(String email) {
        log.info("Inside AuthService : forgotPassword(): {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // Generate a 6-digit random OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        // Save OTP and set Expiry to 10 minutes from now
        user.setResetOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send Email
        emailService.sendPasswordResetEmail(user.getEmail(), otp);
    }

    // Verify OTP and Update Password
    public void resetPassword(String email, String otp, String newPassword) {
        log.info("Inside AuthService : resetPassword()");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // Check if OTP matches
        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Check if OTP is expired
        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Update with new encoded password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear OTP fields for security after successful reset
        user.setResetOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
    }
}
