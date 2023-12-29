package com.stg.service.dto.insurance;

import com.stg.utils.InstallmentPopup;
import com.stg.utils.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class InsurancePaymentListDTO {
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

    private String paymentTime;

    private String totalFee;

    private String periodicFee;

    private String icCode;

    private String icName;

    private String supportCode;

    private String supportName;

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

    private String micType;

    private String micInsuranceFee;

    private String micContractNum;

    private String micFtNumber;

    private String baasMicStatus;

    private String installmentErrorCode; // Ma loi dang ky tra gop

    private InstallmentPopup installmentPopup;

    private String cif;

    private String rmPhoneNumber;

    private String customerLastUpdated;

    private String managingUnit;

    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }

}
