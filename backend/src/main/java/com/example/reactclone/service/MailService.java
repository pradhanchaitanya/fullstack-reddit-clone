package com.example.reactclone.service;

import com.example.reactclone.exception.RedditException;
import com.example.reactclone.model.NotificationEmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private final static String STR_MAIL_TEMPLATE = "mail-template";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private String build(String message) {
        Context context = new Context();
        context.setVariable("message", message);
        return this.templateEngine.process(STR_MAIL_TEMPLATE, context);
    }

    @Async
    void sendMail(final NotificationEmail notificationEmail) {
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
//            helper.setFrom(senderEmail);
            helper.setFrom("project.dummymailer@gmail.com");
            helper.setTo(notificationEmail.recipient());
            helper.setSubject(notificationEmail.subject());
            helper.setText(
                    build(notificationEmail.body())
            );
        };

        try {
            mailSender.send(mimeMessagePreparator);
            log.info("Email - {}", notificationEmail);
        } catch (MailException e) {
            throw new RedditException(e);
        }
    }
}
