package com.stg.service.dto.insurance;

import com.stg.service.dto.customer.CustomerDto;
import com.stg.service.dto.external.ControlStateEnum;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class InsurancePaymentDto {

    private Long id;
    private CustomerDto customer;
    //tổng phí bảo hiểm
    private String totalFee;
    private String mbalInsuranceFee;
    private String micInsuranceFee;
    //thời gian đóng phí
    private String feePaymentTime;
    //định kỳ đóng phí
    private String periodicFee;
    private boolean autoPay;
    private Boolean autoPayRegistered;
    private String autoPayStatus;
    private boolean installment;
    private String rmName;
    private String rmCode;
    private String rmEmail;
    private String rmPhoneNumber;
    private String presenterPhone;
    private String rootAccount;
    private String amount;
    private String paymentTime;
    private String transactionId;
    private String transactionType;
    private String tranStatus; // trạng thái thanh toán
    private String presenter; // Thông tin ngươi giới thiệu

    private ControlStateEnum controlState; // trạng thái đối soát

    private String icName;
    private String icCode;
    private String supportName;
    private String supportCode;
    private String supportEmail;
    private String supportPhoneNumber;
    private String departmentName;
    private String departmentCode;
    private String branchName;
    private String branchCode;
    private String mbalAppNo;
    private String micContractNum; // GCNBH MIC COMBO

    private AutoDebitPaymentDto autoDebitPayment;

    private String fundingSource;
    private String installmentStatus;
    private String period;
    private String feesPayable;
    private String mbFt;
    private Long insuranceRequestId;

    public void setPaymentTime(LocalDateTime dateTime) {
        this.paymentTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, dateTime);
    }

    private String managingUnit;

    public boolean isMBGroup() {
        return StringUtils.hasText(managingUnit);
    }
}
