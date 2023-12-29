package com.stg.service;

import com.stg.utils.EmailType;
import com.stg.service.dto.email.InputEmailDto;

public interface ThymeleafService {
    String getContent(EmailType emailType, InputEmailDto inputEmailDTO);
}
