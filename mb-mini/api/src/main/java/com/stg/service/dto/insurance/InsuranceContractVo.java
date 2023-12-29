package com.stg.service.dto.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InsuranceContractVo {
    Long getId();

    String getMbId();

    String getFullName();

    String getMbTransactionId();

    String getMbFt();

    String getMicFtNumber();

    String getMicContractNum();

    String getMbalFtNumber();

    String getMbalPolicyNumber();

    String getPackageName();

    String getPackageType();

    String getStrInsuranceFee();

    String getMicType();

    String getMbalType();

    String getMbalAppNo();

    String getSegment();

    String getMbalFeePaymentTime();

    Long getInsuranceRequestId();

    String getMbalPeriodicFeePaymentTime();

    Long getPeriodicPrem(); //Phi BH chinh

    Long getMinTopUp(); // Phi TOPUP

    String getMbalProductName();

    String getCreatedDate(); // Ngày mua bảo hiểm

    String getStatus();

    LocalDate getDueFromDate();

    LocalDate getDueToDate();

    String getPremiumType();

    BigDecimal getDueAmount();

    String getPhName();

}
