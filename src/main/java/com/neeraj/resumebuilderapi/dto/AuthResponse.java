package com.neeraj.resumebuilderapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {


    private String id;
    private String name;
    private String email;
    private String subscriptionPlan;
    private String profileImageUrl;
    private Boolean emailVerified;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
