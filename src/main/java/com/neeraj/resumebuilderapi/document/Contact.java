package com.neeraj.resumebuilderapi.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "inquiries")
@Builder
public class Contact {

    @Id
    @JsonProperty("_id")
    private String id;
    private String fullName;
    private String email;
    private String subject;
    private String message;
}
