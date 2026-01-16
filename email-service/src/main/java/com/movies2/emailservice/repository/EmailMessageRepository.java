package com.movies2.emailservice.repository;

import com.movies2.emailservice.model.EmailMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EmailMessageRepository extends ElasticsearchRepository<EmailMessage, String> {
    List<EmailMessage> findTop100ByStatusOrderByLastAttemptAtAsc(EmailMessage.Status status);
}
