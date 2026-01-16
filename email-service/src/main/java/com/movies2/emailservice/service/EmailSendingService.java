package com.movies2.emailservice.service;

import com.movies2.email.EmailSendCommand;
import com.movies2.emailservice.model.EmailMessage;
import com.movies2.emailservice.repository.EmailMessageRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailSendingService {

    private final JavaMailSender mailSender;
    private final EmailMessageRepository repo;

    public EmailSendingService(JavaMailSender mailSender, EmailMessageRepository repo) {
        this.mailSender = mailSender;
        this.repo = repo;
    }

    public EmailMessage saveIncoming(EmailSendCommand cmd) {
        EmailMessage msg = new EmailMessage();
        msg.setSubject(cmd.subject());
        msg.setContent(cmd.content());
        msg.setRecipients(cmd.recipients());
        msg.setStatus(EmailMessage.Status.NEW);
        msg.setAttempt(0);
        msg.setCreatedAt(Instant.now());
        msg.setLastAttemptAt(null);
        return repo.save(msg);
    }

    public void trySend(EmailMessage msg) {
        msg.setAttempt(msg.getAttempt() + 1);
        msg.setLastAttemptAt(Instant.now());

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(msg.getRecipients().toArray(String[]::new));
            mail.setSubject(msg.getSubject());
            mail.setText(msg.getContent());

            mailSender.send(mail);

            msg.setStatus(EmailMessage.Status.SENT);
            msg.setErrorMessage(null);
        } catch (Exception ex) {
            msg.setStatus(EmailMessage.Status.FAILED);
            msg.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        repo.save(msg);
    }
}
