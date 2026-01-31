package com.movies2.emailservice;

import com.movies2.email.EmailSendCommand;
import com.movies2.emailservice.model.EmailMessage;
import com.movies2.emailservice.repository.EmailMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = {"email.send"})
@ActiveProfiles("test")
class KafkaListenerIT {

    @Autowired
    KafkaTemplate<String, EmailSendCommand> kafkaTemplate;

    @MockBean
    EmailMessageRepository repo;

    @MockBean
    org.springframework.mail.javamail.JavaMailSender mailSender;

    @Test
    void shouldConsumeKafkaMessageAndSaveToRepository() {
        EmailSendCommand cmd = new EmailSendCommand(
                "Hello",
                "Test content",
                List.of("user@test.com")
        );

        kafkaTemplate.send("email.send", cmd);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

        verify(repo, timeout(3000).times(1)).save(captor.capture());

        EmailMessage saved = captor.getValue();
        assertThat(saved.getSubject()).isEqualTo("Hello");
        assertThat(saved.getContent()).isEqualTo("Test content");
        assertThat(saved.getRecipients()).contains("user@test.com");
    }
}
