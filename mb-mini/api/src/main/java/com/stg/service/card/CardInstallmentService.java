package com.stg.service.card;

import com.stg.entity.InsurancePayment;
import com.stg.service.dto.baas.ManualRequest;
import com.stg.service.dto.baas.InstallmentManualResp;
import com.stg.service.dto.card.*;

public interface CardInstallmentService {
    void createInstallmentNoOtpProcess(String retRefNumber, String period, String fundingSource, InsurancePayment insurancePayment);

    GetInstFeeResponse getInsFee(GetInstFeeRequest getInsFeeRequest);

    CommonCardResponse createInstallmentNoOtp(CreateInstallElementNoOtpRequest request);

    CommonCardResponse createInstallElementOtp(CreateInstallElementOtpRequest request);

    CommonCardResponse createInstallElementOtpConfirm(CreateInstallElementOtpConfirmRequest request);

    CommonCardResponse getInstallElementResult(GetInstallElementResultRequest request);

    CommonCardResponse getInstInfo(GetInstInfoRequest request);

    InstallmentManualResp createInstallmentNoOtpManual(ManualRequest reqDto);

}
