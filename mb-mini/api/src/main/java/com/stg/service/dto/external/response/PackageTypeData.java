package com.stg.service.dto.external.response;

import com.stg.service.dto.external.MicInsuranceBenefitDto;
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
public class PackageTypeData {

    private String id;
    private String name;
    private String photo;
    private String backgroundImage;
    private BigDecimal insuranceFee;
    private String deathBenefit;
    private String strInsuranceFee;
    private String voucherAccount;
    private String packageCode;
    private int orderNo;
    private String productCode;
    private String productName;
    private String payfrqCd;
    private String insuranceFeeStr;
    private String type;
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

    private MicInsuranceBenefitDto micInsuranceBenefit;

}
