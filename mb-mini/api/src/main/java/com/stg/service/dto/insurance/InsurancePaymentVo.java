package com.stg.service.dto.insurance;

public interface InsurancePaymentVo {
    Long getId();

    String getMbId();

    String getFullName();

    String getTranStatus();

    String getMbTransactionId();

    String getMbFt();

    String getMicInsuranceFee();

    String getMicContractNum();

    String getMicFtNumber();

    String getBaasMicStatus();

    String getMbalInsuranceFee();

    String getMbalAppNo();

    String getMbalFtNumber();

    String getBaasMbalStatus();

    String getControlState();

    String getMicType();

    String getMbalType();

    String getRmCode();

    String getRmName();
    String getRmEmail();
    String getRmPhoneNumber();

    String getPaymentTime();

    String getTotalFee();

    String getPeriodicFee();

    String getIcCode();

    String getIcName();

    String getSupportCode();

    String getSupportName();
    String getSupportEmail();
    String getSupportPhoneNumber();

    String getBranchCode();

    String getBranchName();

    String getDepartmentCode();

    String getDepartmentName();

    String getMbalFeePaymentTime();

    boolean isNormal();
    boolean isAutoPay();
    Boolean getAutoPayRegistered();
    String getAutoPayStatus();
    String getInstallmentStatus();
    String getPeriod();
    String getPeriodicConversionFee();
    String getFeesPayable();
    String getFundingSource();

    String getPackageType();
    Long getInsuranceRequestId();

    String getInstallmentErrorCode();
    String getCif();
    String getManagingUnit();

    String getCustomerLastUpdated();

}
