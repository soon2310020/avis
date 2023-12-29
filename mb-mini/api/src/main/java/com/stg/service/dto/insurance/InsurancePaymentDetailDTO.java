package com.stg.service.dto.insurance;

import com.stg.utils.InstallmentPopup;
import com.stg.utils.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class InsurancePaymentDetailDTO {
    private Long id;

    private String mbId;

    private String fullName;

    private String tranStatus;

    private String mbTransactionId;

    private String mbFt;

    private String mbalInsuranceFee;

    private String mbalAppNo;

    private String mbalFtNumber;

    private String baasMbalStatus;

    private String controlState;

    private String mbalType;

    private String rmCode;

    private String rmName;
    private String rmEmail;
    private String rmPhoneNumber;

    private String paymentTime;

    private String totalFee;

    private String periodicFee;

    private String icCode;

    private String icName;

    private String supportCode;

    private String supportName;
    private String supportEmail;
    private String supportPhoneNumber;


    private String branchCode;

    private String branchName;

    private String departmentCode;

    private String departmentName;

    private String mbalFeePaymentTime;

    private String fundingSource;

    private PaymentType paymentType;
    private boolean autoPay;
    private Boolean autoPayRegistered;
    private String autoPayStatus;
    private String installmentStatus;
    private String period;
    private String feesPayable;

    private String packageType;
    private Long insuranceRequestId;

    private String installmentErrorCode; // Ma loi dang ky tra gop

    private InstallmentPopup installmentPopup;

    private String micType;

    private String cif;

    private String customerLastUpdated;

    private List<MicInfo> listMicInfo;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MicInfo {
        private String micInsuranceFee;

        private String micContractNum;

        private String micFtNumber;

        private String baasMicStatus;
    }
}
