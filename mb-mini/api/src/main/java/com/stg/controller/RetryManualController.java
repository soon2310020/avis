package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.AutoDebitPaymentService;
import com.stg.service.BaasService;
import com.stg.service.dto.baas.InsurancePaymentRetryReq;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.stg.utils.Endpoints.RETRY_MANUAL;

@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "RetryManual Apis")
@RestController
public class RetryManualController {
    private final BaasService baasService;
    private final AutoDebitPaymentService autoDebitPaymentService;

    @PostMapping(RETRY_MANUAL.REGISTER_AUTO_DEBIT)
    @ResponseStatus(HttpStatus.OK)
    public void retryRegisterAutoDebitManual(@AuthenticationPrincipal CustomUserDetails user,
                                                                  @Valid @RequestBody InsurancePaymentRetryReq reqDto) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));
        autoDebitPaymentService.retryRegisterAutoDebitManual(reqDto);
    }

}
