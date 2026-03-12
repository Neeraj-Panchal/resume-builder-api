package com.neeraj.resumebuilderapi.repository;

import com.neeraj.resumebuilderapi.document.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends MongoRepository<Contact, String> {
}
