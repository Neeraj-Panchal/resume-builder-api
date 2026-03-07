package com.neeraj.resumebuilderapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.neeraj.resumebuilderapi.document.Resume;
import com.neeraj.resumebuilderapi.dto.AuthResponse;
import com.neeraj.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final AuthService authService;
    private final ResumeRepository resumeRepository;

    //reusable method used to upload images
    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {
        Map<String, Object> imageUploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image"));
        log.info("Inside FileUploadService - uploadSingleImage() {}", imageUploadResult.get("secure_url").toString());
        return Map.of("imageUrl",imageUploadResult.get("secure_url").toString());
    }

    public Map<String, String> uploadResumeImages(String resumeId, Object principal, MultipartFile thumbnail, MultipartFile profileImage) throws IOException {

        //step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

        //step 2 : get the existing resume
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId )
                .orElseThrow(()-> new RuntimeException("Profile not found"));

        //step 3 : upload the resume images and set the resume
        Map<String, String> returnValue = new HashMap<>();
        Map<String, String> uploadResult;

        //if user sets the thumbnail only then it is called if not then it is stay at rest
        if(Objects.nonNull(thumbnail)){
            uploadResult = uploadSingleImage(thumbnail);
            existingResume.setThumbnailLink(uploadResult.get("imageUrl"));
            returnValue.put("thumbnailLink", uploadResult.get("imageUrl"));
        }

        if(Objects.nonNull(profileImage)){
            uploadResult = uploadSingleImage(profileImage);
            if (Objects.isNull(existingResume.getProfileInfo())){
                existingResume.setProfileInfo(new Resume.ProfileInfo());
            }
            existingResume.getProfileInfo().setProfilePreviewUrl(uploadResult.get("imageUrl"));
            returnValue.put("profilePreviewUrl", uploadResult.get("imageUrl"));
        }


        //step 4 : update the details into database
        resumeRepository.save(existingResume);
        returnValue.put("message","Images uploaded successfully");

        return returnValue;
    }
}
