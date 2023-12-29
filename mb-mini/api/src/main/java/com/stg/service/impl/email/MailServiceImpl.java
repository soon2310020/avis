package com.stg.service.impl.email;

import com.stg.utils.EmailType;
import com.stg.errors.SendEmailException;
import com.stg.service.MailService;
import com.stg.service.ThymeleafService;
import com.stg.service.dto.email.InputEmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.EnumMap;
import java.util.Map;

@Service
public class MailServiceImpl implements MailService {

    private static final String CONTENT_TYPE_TEXT_HTML = "text/html;charset=\"utf-8\"";

    private final ThymeleafService thymeleafService;
    private final JavaMailSender javaMailSender;
    private static Map<EmailType, String> subjectTitleMap = new EnumMap<>(EmailType.class);

    @Value("${spring.mail.username}")
    private String mailUsername;

    static {
        subjectTitleMap.put(EmailType.FORGOT_PASSWORD, "New Password For User");
        subjectTitleMap.put(EmailType.GENERATE_ACCOUNT_PASSWORD, "Generate Password For New Account");
        subjectTitleMap.put(EmailType.RESET_PASSWORD, "Reset Password");
    }

    public MailServiceImpl(ThymeleafService thymeleafService, JavaMailSender javaMailSender) {
        this.thymeleafService = thymeleafService;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Async
    public void sendMail(EmailType emailType, InputEmailDto inputEmailDTO) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(inputEmailDTO.getEmailTo()));
            message.setFrom(new InternetAddress(mailUsername));
            message.setSubject(subjectTitleMap.get(emailType));
            message.setContent(thymeleafService.getContent(emailType, inputEmailDTO), CONTENT_TYPE_TEXT_HTML);
        } catch (MessagingException e) {
            throw new SendEmailException("Have exception when send email");
        }
        javaMailSender.send(message);
    }

}
