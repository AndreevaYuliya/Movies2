package com.movies2.emailservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Document(indexName = "email_messages")
public class EmailMessage {

    @Id
    private String id;

    private String subject;
    private String content;
    private List<String> recipients;

    private Status status; // NEW, SENT, FAILED
    private String errorMessage;

    private int attempt;
    private Instant lastAttemptAt;
    private Instant createdAt;

    public enum Status { NEW, SENT, FAILED }
}
