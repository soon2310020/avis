package com.stg.config;

import com.stg.errors.ApplicationException;
import com.stg.errors.ErrorUtil;
import com.stg.errors.dto.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;

import javax.security.auth.login.CredentialExpiredException;

public class CustomWebResponseExceptionTranslator extends DefaultWebResponseExceptionTranslator {

    private static final Logger LOG = LoggerFactory.getLogger(CustomWebResponseExceptionTranslator.class);

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) {
        LOG.info("Translating caught exception: ", e);

        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cache-Control", "no-store");
        headers.set("Pragma", "no-cache");

        ErrorDto errorDto = ErrorUtil.getDefaultErrorDto();

        if (e instanceof CredentialExpiredException) {
            errorDto.setErrorCode(((ApplicationException) e).getErrorCode());
        }

        @SuppressWarnings("unchecked")
        ResponseEntity result = new ResponseEntity(errorDto, headers, httpStatus);

        return result;
    }

}