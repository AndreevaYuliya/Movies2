package com.movies2.emailservice.kafka;

import com.movies2.email.EmailSendCommand;
import com.movies2.emailservice.service.EmailSendingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailCommandListener {

    private final EmailSendingService service;

    public EmailCommandListener(EmailSendingService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.group-id}")
    public void onMessage(EmailSendCommand cmd) {
        var saved = service.saveIncoming(cmd);
        service.trySend(saved);
    }
}
