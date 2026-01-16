package com.movies2.service;

import com.movies2.email.EmailSendCommand;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailProducer {

    private final KafkaTemplate<String, EmailSendCommand> kafka;

    public EmailProducer(KafkaTemplate<String, EmailSendCommand> kafka) {
        this.kafka = kafka;
    }

    public void sendCreatedNotification(String to) {
        kafka.send("email.send", new EmailSendCommand(
                "New entity created",
                "A new entity was created in Movies2",
                List.of(to)
        ));
    }
}
