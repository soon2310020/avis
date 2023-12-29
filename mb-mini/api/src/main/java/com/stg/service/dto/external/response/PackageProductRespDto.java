package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PackageProductRespDto {

    private String packageCode;
    private String deathNoAccidentFrom;
    private String strDeathNoAccidentFrom;
    private String deathNoAccidentTo;
    private String strDeathNoAccidentTo;
    private String deathAccident;
    private String strDeathAccident;
    private String deathAccidentYesTraffic;
    private String deathAccidentNoTraffic;
    private String supInpatientHospitalFee;
    private String insuranceFee;
    private String baseInsuranceFee;
    private String topupInsuranceFee;
    private String payFrequency;
    private String timeFrequency;
    private int amount;


    public void setStrDeathNoAccidentFrom(String strDeathNoAccidentFrom) {
        this.strDeathNoAccidentFrom = strDeathNoAccidentFrom == null ? "" : strDeathNoAccidentFrom.replace(",", ".");
    }

    public void setStrDeathNoAccidentTo(String strDeathNoAccidentTo) {
        this.strDeathNoAccidentTo = strDeathNoAccidentTo == null ? "" : strDeathNoAccidentTo.replace(",", ".");
    }

    public void setStrDeathAccident(String strDeathAccident) {
        this.strDeathAccident = strDeathAccident == null ? "" : strDeathAccident.replace(",", ".");
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode == null ? "": packageCode.replace(",", ".");
    }

    public void setDeathNoAccidentFrom(String deathNoAccidentFrom) {
        this.deathNoAccidentFrom = deathNoAccidentFrom == null ? "": deathNoAccidentFrom.replace(",", ".");
    }

    public void setDeathNoAccidentTo(String deathNoAccidentTo) {
        this.deathNoAccidentTo = deathNoAccidentTo == null ? "": deathNoAccidentTo.replace(",", ".");
    }

    public void setDeathAccident(String deathAccident) {
        this.deathAccident = deathAccident.replace(",", ".");
    }

    public void setDeathAccidentYesTraffic(String deathAccidentYesTraffic) {
        this.deathAccidentYesTraffic = deathAccidentYesTraffic.replace(",", ".");
    }

    public void setDeathAccidentNoTraffic(String deathAccidentNoTraffic) {
        this.deathAccidentNoTraffic = deathAccidentNoTraffic.replace(",", ".");
    }

    public void setSupInpatientHospitalFee(String supInpatientHospitalFee) {
        this.supInpatientHospitalFee = supInpatientHospitalFee.replace(",", ".");
    }

    public void setInsuranceFee(String insuranceFee) {
        this.insuranceFee = insuranceFee.replace(",", ".");
    }

    public void setBaseInsuranceFee(String baseInsuranceFee) {
        this.baseInsuranceFee = baseInsuranceFee.replace(",", ".");
    }

    public void setTopupInsuranceFee(String topupInsuranceFee) {
        this.topupInsuranceFee = topupInsuranceFee.replace(",", ".");
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency.replace(",", ".");
    }

    public void setTimeFrequency(String timeFrequency) {
        this.timeFrequency = timeFrequency.replace(",", ".");
    }
}
