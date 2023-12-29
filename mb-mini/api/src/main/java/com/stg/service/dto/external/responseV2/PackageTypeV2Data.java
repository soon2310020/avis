package com.stg.service.dto.external.responseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PackageTypeV2Data {

    private String code;    // Mã gói
    private String groupName;   // Tên gói
    private String expiredDate; // Ngày hết hạn
    private Long sumAssured;    //Số tiền bảo hiểm
    private Long accidentBenefit;    //Quyền lợi bảo hiểm tử vong/thương tật.. do tai nạn
    private Long inpatientTreatmentBenefit;    //Quyền lợi bảo hiểm điều trị nội trú do ốm đau bệnh tật
    private String paymentPeriod;   // Định kì đóng phí: ONCE|ANNUAL
    private String type;   // Loại gói: ULLP_PROTECT|ULLP_INVEST|ULLP_PROTECT_HSCR
    private BigDecimal insuranceFee;   // Phí bảo hiểm

    private String id;
    private String photo;
    private String backgroundImage;
    private String deathBenefit;
    private String strInsuranceFee;
    private String voucherAccount;
    private String packageCode;
    private int orderNo;
    private String productCode;
    private String productName;
    private String payfrqCd;
    private String insuranceFeeStr;
    private String mic;
    private String cashback;
    private String strDeathBenefit;
    private List<LstUlsp> lstUlsp;

    private String mixPackageName;
    private BigDecimal totalInsuranceBenefit;
    private String strTotalInsuranceBenefit;
    private BigDecimal mixInsuranceFee;
    private BigDecimal micInsuranceFee;
    private String strMixInsuranceFee;
    private Integer order;

    public void setDeathBenefit(String deathBenefit) {
        this.deathBenefit = deathBenefit.replace(",", ".");
    }

    public void setStrInsuranceFee(String strInsuranceFee) {
        this.strInsuranceFee = strInsuranceFee.replace(",", ".");
    }

    public void setInsuranceFeeStr(String insuranceFeeStr) {
        this.insuranceFeeStr = insuranceFeeStr.replace(",", ".");
    }

    public void setStrDeathBenefit(String strDeathBenefit) {
        this.strDeathBenefit = strDeathBenefit.replace(",", ".");
    }

    public void setStrMixInsuranceFee(String strMixInsuranceFee) {
        this.strMixInsuranceFee = strMixInsuranceFee == null ? "": strMixInsuranceFee.replace(",", ".");
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LstUlsp {
        private String packageCode;
        private String display;
    }

    private MicInsuranceBenefitV2Dto micInsuranceBenefit;

}
