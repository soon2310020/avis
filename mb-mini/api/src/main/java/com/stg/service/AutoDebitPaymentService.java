package com.stg.service;

import com.stg.entity.InsurancePayment;
import com.stg.entity.customer.Customer;
import com.stg.service.dto.baas.InsurancePaymentRetryReq;
import com.stg.service.dto.external.request.MbCallBackTransactionReqDto;
import com.stg.service.dto.insurance.RegisterAutoDebitStatusDto;
import org.springframework.lang.Nullable;

public interface AutoDebitPaymentService {
    RegisterAutoDebitStatusDto createAutoDebitNonOTP(Customer customer,
                                                     InsurancePayment insurancePayment,
                                                     @Nullable MbCallBackTransactionReqDto mbCallBackData);

    void retryRegisterAutoDebitManual(InsurancePaymentRetryReq reqDto);
}
