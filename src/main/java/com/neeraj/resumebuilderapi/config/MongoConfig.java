package com.neeraj.resumebuilderapi.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.neeraj.resumebuilderapi.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {


    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/resumebuilder}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {

        return "resumebuilder";
    }

    @Override
    public MongoClient mongoClient() {

        return MongoClients.create(mongoUri);
    }
}