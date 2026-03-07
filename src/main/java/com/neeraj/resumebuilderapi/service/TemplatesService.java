package com.neeraj.resumebuilderapi.service;

import com.neeraj.resumebuilderapi.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neeraj.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatesService {

    private final AuthService authService;

    public Map<String, Object> getTemplates(Object principal){
        //step 1 : get the current profile
        AuthResponse authResponse = authService.getProfile(principal);

        //step 2 : get the available templates based on subscription
        List<String> availableTemplates;

        Boolean isPremium = PREMIUM.equalsIgnoreCase(authResponse.getSubscriptionPlan());

        if (isPremium){
            availableTemplates = List.of("01","02","03");
        }else {
            availableTemplates = List.of("01");
        }

        //step 3 : add the data into map
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("availableTemplates", availableTemplates);
        templateData.put("allTemplates", List.of("01","02","03"));
        templateData.put("subscriptionPlan", authResponse.getSubscriptionPlan());
        templateData.put("isPremium", isPremium);

        //step 4 : return the restrictions
        return templateData;
    }
}
