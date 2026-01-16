package com.movies2.emailservice.scheduler;

import com.movies2.emailservice.model.EmailMessage;
import com.movies2.emailservice.repository.EmailMessageRepository;
import com.movies2.emailservice.service.EmailSendingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FailedEmailRetryJob {

    private final EmailMessageRepository repo;
    private final EmailSendingService service;

    public FailedEmailRetryJob(EmailMessageRepository repo, EmailSendingService service) {
        this.repo = repo;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "PT5M") // ISO-8601, 5 minutes
    public void retryFailed() {
        var failed = repo.findTop100ByStatusOrderByLastAttemptAtAsc(EmailMessage.Status.FAILED);
        for (var msg : failed) {
            service.trySend(msg);
        }
    }
}
