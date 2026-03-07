package com.neeraj.resumebuilderapi.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.neeraj.resumebuilderapi.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        // Yeh line default 'test' database ko block kar degi
        return "resumebuilder";
    }

    @Override
    public MongoClient mongoClient() {
        // Aapka local connection setup
        return MongoClients.create("mongodb://localhost:27017");
    }
}