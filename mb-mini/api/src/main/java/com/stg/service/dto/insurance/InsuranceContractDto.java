package com.stg.service.dto.insurance;

import com.stg.entity.AutoDebitPayment;
import com.stg.entity.InsurancePayment;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import com.stg.utils.FundingSource;
import com.stg.utils.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractDto {

    private Long id;

    private CustomerDto customer;

    private Constants.PackageType packageType;

    private MbalPackageDto mbalPackage;

    private MicPackageDto micPackage;

    private InsurancePackageDto insurancePackage;

    @Schema(description = "Ngay MBAL phát hành hop dong", example = "20/01/2023")
    private String mbalIssueDate;

    @Schema(description = "Ngày bắt đầu mua bảo hiểm", example = "20/01/2023")
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

    private boolean checkMicIssueAfterNow;

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

    private List<AdditionalProductDTO> customerAdditionalProductDTOS;

    private PrimaryInsuredDTO primaryInsuredDTO;

    private PrimaryProductDTO primaryProductDTO;

//    private List<AdditionalProductDTO> primaryInsuredAdditionalProductDTOS;

    private List<AdditionalInsuredDTO> additionalAssuredOutputs;

    private List<BeneficiaryDTO> beneficiaryDTOS;

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

    public void setMbalIssueDate(String issueDate) {
        this.mbalIssueDate = issueDate;
    }

    public void setMicIssueDate(LocalDateTime micIssueDate) {
        this.micIssueDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, micIssueDate);
    }

    private Insured insured; // người được bảo hiểm

    private List<String> additionalProductNames; // tên sản phẩm bổ trợ

    private String startDate; //Ngày bắt đầu hiệu lực

    private String endDate; //Ngày hết hạn

    private String sumInsured; //Số tiền bảo hiểm

    private String sumInsuredAdditional; //Số tiền bảo hiểm bổ trợ

    private String source;

    private String coverageYear;

    private String agentCode;

    private String agentName;

    private String agentNum;

    private String premiumType; //Loại phí: OVERDUE: Phí quá hạn, DUE: Phí tới hạn, NDUE: Kỳ phí sắp tới (Next Due)

    private String dueFromDate;

    private String dueToDate;

    private BigDecimal dueAmount; //số tiền phí

    private String rmCode;
    private String rmName;
    private String rmEmail;
    private String rmPhoneNumber;
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

    private String activeDate;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Insured {
        private String fullName;

        private String idCardType;

        private String identification;

        private String phone;

        private String address;

        private String idIssuedPlace; // Nơi đăng ký identification

        private List<IdentificationDetailDto> identifications;
    }

    public void setInsurancePayment(InsurancePayment insurancePayment) {
        if (insurancePayment == null || insurancePayment.getId() == null) return;

        this.setAutoPay(insurancePayment.isAutoPay());
        AutoDebitPayment autoDebitPayment = insurancePayment.getAutoDebitPayment();
        if (autoDebitPayment != null) {
            this.setAutoPayRegistered(autoDebitPayment.isRegistered());
            this.setAutoPayStatus(autoDebitPayment.getStatusMessage());
        }

        this.setPaymentType(insurancePayment.isNormal() ? PaymentType.NORMAL : PaymentType.INSTALLMENT);
        this.setInstallmentStatus(insurancePayment.getInstallmentStatus());
        this.setPeriod(insurancePayment.getPeriod());
        this.setFundingSource(insurancePayment.getFundingSource());

        // la tra gop
        if (!insurancePayment.isNormal() && insurancePayment.getTotalFee() != null && insurancePayment.getPeriod() != null && insurancePayment.getPeriodicConversionFee() != null) {
            double totalFee = Double.parseDouble(insurancePayment.getTotalFee().replace(".", "").split(" VND")[0]);
            int periodFee = Integer.parseInt(insurancePayment.getPeriod());
            BigDecimal monthlyPaymentFee = BigDecimal.valueOf(totalFee * (Double.parseDouble(insurancePayment.getPeriodicConversionFee()) / 100) / periodFee);
            monthlyPaymentFee = monthlyPaymentFee.add(BigDecimal.valueOf(totalFee / periodFee)).setScale(2, RoundingMode.HALF_UP);
            this.setFeesPayable(monthlyPaymentFee.toString());
            this.setInstallmentErrorCode(insurancePayment.getInstallmentErrorCode());
        }

        this.setRmCode(insurancePayment.getRmCode());
        this.setRmName(insurancePayment.getRmName());
        this.setRmEmail(insurancePayment.getRmEmail());
        this.setRmPhoneNumber(insurancePayment.getRmPhoneNumber());
        this.setIcCode(insurancePayment.getIcCode());
        this.setIcName(insurancePayment.getIcName());
        this.setSupportCode(insurancePayment.getSupportCode());
        this.setSupportName(insurancePayment.getSupportName());
        this.setSupportEmail(insurancePayment.getSupportEmail());
        this.setSupportPhoneNumber(insurancePayment.getSupportPhoneNumber());
        this.setBranchCode(insurancePayment.getBranchCode());
        this.setBranchName(insurancePayment.getBranchName());
        this.setDepartmentCode(insurancePayment.getDepartmentCode());
        this.setDepartmentName(insurancePayment.getDepartmentName());
    }

}
