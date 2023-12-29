package com.stg.service;

import com.stg.utils.EmailType;
import com.stg.service.dto.email.InputEmailDto;

public interface MailService {
    void sendMail(EmailType emailType, InputEmailDto inputEmailDto);
}
