package com.stg.service.dto.insurance;

public interface InsuranceContractThirdPartyVo {
    Long getId();

    String getMbId();

    String getFullName();

    String getMbalAppNo();

    String getMbalPolicyNumber();

    String getMicContractNum();

    String getProductName();

    String getStrInsuranceFee(); //Số tiền bảo hiểm

    String getMbalFeePaymentTime();//Thời hạn bảo hiểm

    String getStatus();

    String getSource();

}
