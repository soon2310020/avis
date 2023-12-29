package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GenerateQuoteRespDto {

    private String insuranceFee;
    private String baseInsuranceFee;
    private String topupInsuranceFee;
    private String hscrFee;
    private Double amount;
    private String payFrequency;
    private String timeFrequency;
    @JsonProperty("hsTimeFrequency")
    private String hsTimeFrequency;

    public void setInsuranceFee(String insuranceFee) {
        this.insuranceFee = insuranceFee == null ? "": insuranceFee.replace(",", ".");
    }

    public void setBaseInsuranceFee(String baseInsuranceFee) {
        this.baseInsuranceFee = baseInsuranceFee == null ? "": baseInsuranceFee.replace(",", ".");
    }

    public void setHscrFee(String hscrFee) {
        this.hscrFee = hscrFee == null ? "": hscrFee.replace(",", ".");
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency.replace(",", ".");
    }

}
