package com.movies2.emailservice;

import com.movies2.emailservice.model.EmailMessage;
import com.movies2.emailservice.repository.EmailMessageRepository;
import com.movies2.emailservice.service.EmailSendingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailSendingServiceIT {

    @MockBean
    JavaMailSender mailSender;

    @Autowired
    EmailMessageRepository repo;

    @Autowired
    EmailSendingService service;

    @Test
    void trySend_success_setsStatusSent_andClearsError() {
        EmailMessage msg = new EmailMessage();
        msg.setSubject("Hello");
        msg.setContent("Body");
        msg.setRecipients(List.of("a@test.com"));
        msg.setStatus(EmailMessage.Status.NEW);
        msg.setAttempt(0);

        msg = repo.save(msg);

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        service.trySend(msg);

        EmailMessage saved = repo.findById(msg.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EmailMessage.Status.SENT);
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getAttempt()).isEqualTo(1);
        assertThat(saved.getLastAttemptAt()).isNotNull();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void trySend_fail_setsStatusFailed_andStoresError() {
        EmailMessage msg = new EmailMessage();
        msg.setSubject("Hello");
        msg.setContent("Body");
        msg.setRecipients(List.of("a@test.com"));
        msg.setStatus(EmailMessage.Status.NEW);
        msg.setAttempt(0);

        msg = repo.save(msg);

        doThrow(new RuntimeException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        service.trySend(msg);

        EmailMessage saved = repo.findById(msg.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EmailMessage.Status.FAILED);
        assertThat(saved.getErrorMessage()).contains("RuntimeException").contains("smtp down");
        assertThat(saved.getAttempt()).isEqualTo(1);
        assertThat(saved.getLastAttemptAt()).isNotNull();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
