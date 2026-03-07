package com.neeraj.resumebuilderapi.repository;

import com.neeraj.resumebuilderapi.document.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends MongoRepository<Resume, String> {

    List<Resume> findByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<Resume> findByUserIdAndId(String userId, String id);

}
