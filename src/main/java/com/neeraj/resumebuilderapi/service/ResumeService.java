package com.neeraj.resumebuilderapi.service;

import com.neeraj.resumebuilderapi.document.Resume;
import com.neeraj.resumebuilderapi.dto.AuthResponse;
import com.neeraj.resumebuilderapi.dto.CreateResumeRequest;
import com.neeraj.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final AuthService authService;

    public Resume createResume(CreateResumeRequest request, Object principalObject) {
        //step 1 : create resume object
        Resume newResume = new Resume();

        //step 2 : get the current profile
        AuthResponse response = authService.getProfile(principalObject);

        //step 3 : update the resume object
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

        //step 4 : set default data for resume
        setDefaultResumeData(newResume);

        //step 5 : save the resume data
        return resumeRepository.save(newResume);
    }

    private void setDefaultResumeData(Resume newResume) {
        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperiences(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkills(new ArrayList<>());
        newResume.setProjects(new ArrayList<>());
        newResume.setCertifications(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<Resume> getUserResumes(Object principal) {
        //step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

        //step 2 : call the respository finder method
        List<Resume> resumes =  resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        //step 3 : return the response
        return resumes;
    }

    public Resume getResumeById(String resumeId, Object principal) {
        //step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

        //step 2 : call the repository finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        //step 3 : return the result
        return existingResume;
    }

    public Resume updateResume(String resumeId, Resume updatedData, Object principal) {
        //step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

        //step 2 : call the respository finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        //step 3 : update the new data
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setSkills(updatedData.getSkills());
        existingResume.setProjects(updatedData.getProjects());
        existingResume.setCertifications(updatedData.getCertifications());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setInterests(updatedData.getInterests());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperiences(updatedData.getWorkExperiences());

        //step 4 : update the details into database
        return resumeRepository.save(existingResume);
    }

    public void deleteResume(String resumeId, Object principal) {

        //step 1 : get the current profile
        AuthResponse response = authService.getProfile(principal);

        //step 2 : call the repository finder method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId )
                .orElseThrow(()-> new RuntimeException("Resume not found"));

        //step 3 : delete the resume
        resumeRepository.delete(existingResume);
    }
}
