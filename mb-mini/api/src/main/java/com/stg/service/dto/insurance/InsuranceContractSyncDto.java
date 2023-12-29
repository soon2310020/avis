package com.stg.service.dto.insurance;

import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.DateUtil;
import com.stg.utils.FundingSource;
import com.stg.utils.PaymentType;
import com.stg.utils.PremiumType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractSyncDto {

    private CustomerDto customer;

    private String fullName;

    private String fullNameT24;

    @Schema(description = "Ngay MBAL phát hành hop dong", required = true, example = "20/01/2023")
    private String mbalIssueDate;

    // tong phi
    private String strInsuranceFee;

    // phi bao hiem MBAL
    private String mbalAmount;

    // So HDBH MBAL
    private String mbalPolicyNumber;

    // So AF.xxx MBAL
    private String mbalAppNo;

    //thời gian đóng phí mbal
    private String mbalFeePaymentTime;

    //định kỳ đóng phí mbal
    private String mbalPeriodicFeePaymentTime;

    private String status;

    private String packageNames;

    private String productNameMbal;

    private String mbTransactionId;

    private String mbFt;

    private String mbalFtNumber;

    private PaymentType paymentType;

    private FundingSource fundingSource;

    private boolean autoPay;

    private Boolean autoPayRegistered;

    private String autoPayStatus;

    private String installmentStatus;

    private String period;

    private String feesPayable;

    private String installmentErrorCode;

    public void setMbalIssueDate(LocalDateTime mbalIssueDate) {
        if (mbalIssueDate != null) {
            this.mbalIssueDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, mbalIssueDate);
        }
    }

    private String startDate; //Ngày bắt đầu hiệu lực

    private String endDate; //Ngày hết hạn

    private String sumInsured; //Số tiền bảo hiểm

    private String source;

    private String coverageYear;

    private String agentCode;

    private String agentName;

    private String agentNum;

    private PremiumType premiumType; //Loại phí: OVERDUE: Phí quá hạn, DUE: Phí tới hạn, NDUE: Kỳ phí sắp tới (Next Due)

    private String dueFromDate;

    private String dueToDate;

    private String dueAmount; //số tiền phí

}
