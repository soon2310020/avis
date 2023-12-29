package com.stg.service.dto.insurance;

import com.stg.utils.BenefitType;
import com.stg.utils.Constants;
import com.stg.utils.PaymentPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdditionalProductDTO {

    private Long id;

    private Long insuranceRequestId;

    private Long customerId;

    private Long primaryInsuredId;

    private Long additionalInsuredId;

    private MicPackageDto micPackage;

    private String bs1;

    private String bs2;

    private String bs3;

    private String bs4;

    //sản phẩm bổ trợ COI_RIDER , ADDR, CIR ,..
    private MbalPackageDto mbalPackage;

    //thời hạn hợp đồng
    private String contractPeriod;

    //thời gian đóng phí
    private String feePaymentTime;

    //số tiền bảo hiểm
    private String amount;

    //đồng ý khấu trừ phí bảo hiểm khi đến hạn thanh toán
    private String agreeDeductPremiumsDue;

    private Constants.PackageType type; // MBAL hoặc MIC

    private String code;

    private Integer policyTerm;

    private Integer premiumTerm;

    private Long sumAssured;

    private PaymentPeriod paymentPeriod;

    private BenefitType insuredBenefit;

    private String beneficiaryName;

    private Long basePremium;

    private Long regBasePrem;

    private String assuredId;

    private String productName;

    private String productType;

    private BigDecimal micFee;

    private BigDecimal micSumBenefit ;

    private String micTransactionId;

    // GCNBH MIC
    private String micContractNum;

}
