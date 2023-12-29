package com.stg.service.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractListDto {
    private Long Id;

    private String mbId;

    private String fullName;

    private String mbTransactionId;

    private String mbFt;

    private String micFtNumber;

    private String micContractNum;

    private String mbalFtNumber;

    private String mbalPolicyNumber;

    private String packageName;

    private String strInsuranceFee;

    private String micType;

    private String mbalType;

    private String mbalAppNo;

    private String mbalFeePaymentTime;

    private String segment;

    private String packageType;

    private Long insuranceRequestId;
}
