package com.stg.service.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractThirdPartyListDto {
    private Long id;

    private String mbId;

    private String fullName;

    private String mbalAppNo;

    private String mbalPolicyNumber;

    private String micContractNum;

    private String productName;

    private String strInsuranceFee; //Số tiền bảo hiểm

    private String mbalFeePaymentTime; //Thời hạn bảo hiểm

    private String status;

    private String source;

}
