package com.neeraj.resumebuilderapi.repository;

import com.neeraj.resumebuilderapi.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    //TO FIND THE USER BY EMAIL IN THE USER COLLECTION
    Optional<User> findByEmail(String email);

    //TO CHECK WHETHER THE EMAIL IS ALREADY EXISTS OR NOT
    Boolean existsByEmail(String email);

    //TO VERIFY THE USER IN THE REGISTRATION PROCESS
    Optional<User> findByVerificationToken(String verificationToken);
}
