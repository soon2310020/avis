package com.stg.service.dto.insurance;

import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
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
public class InsuranceContractMbalDto {

    private Long id;

    private CustomerDto customer;

    private Constants.PackageType packageType;

    private MbalPackageDto mbalPackage;

    private MicPackageDto micPackage;

    private InsurancePackageDto insurancePackage;

    @Schema(description = "Ngay MBAL phát hành hop dong", required = true, example = "20/01/2023")
    private String mbalIssueDate;

    @Schema(description = "Ngày bắt đầu mua bảo hiểm", required = true, example = "20/01/2023")
    private String micIssueDate;

    // tong phi
    private String strInsuranceFee;

    // phi bao hiem MIC
    private String micAmount;

    // phi bao hiem MBAL
    private String mbalAmount;

    // GCNBH MIC
    private String micContractNum;

    // So HDBH MBAL
    private String mbalPolicyNumber;

    // So AF.xxx MBAL
    private String mbalAppNo;

    //thời gian đóng phí mic
    private String micFeePaymentTime;

    //định kỳ đóng phí mic
    private String micPeriodicFee;

    //thời gian đóng phí mbal
    private String mbalFeePaymentTime;

    //định kỳ đóng phí mbal
    private String mbalPeriodicFeePaymentTime;

    private String status;

    private String logo;

    private String packageNames;

    private String productNameMic;

    private String productNameMbal;

    private String segment;
    private String mbTransactionId;
    private String mbFt;
    private String micFtNumber;
    private String mbalFtNumber;

    public void setMbalIssueDate(LocalDateTime mbalIssueDate) {
        if (mbalIssueDate != null) {
            this.mbalIssueDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, mbalIssueDate);
        }
    }

    public void setMicIssueDate(LocalDateTime micIssueDate) {
        this.micIssueDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, micIssueDate);
    }

}
