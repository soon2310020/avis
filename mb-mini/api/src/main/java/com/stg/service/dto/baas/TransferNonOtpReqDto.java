package com.stg.service.dto.baas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TransferNonOtpReqDto {
    @JsonProperty("serviceType")
    private String serviceType;
    @JsonProperty("customerType")
    private String customerType;
    @JsonProperty("customerLevel")
    private String customerLevel;
    @JsonProperty("debitType")
    private String debitType;
    @JsonProperty("debitResourceNumber")
    private String debitResourceNumber;
    @JsonProperty("debitName")
    private String debitName;
    @JsonProperty("creditType")
    private String creditType;
    @JsonProperty("creditResourceNumber")
    private String creditResourceNumber;
    @JsonProperty("creditName")
    private String creditName;
    @JsonProperty("transferType")
    private String transferType;
    @JsonProperty("bankCode")
    private String bankCode;
    @JsonProperty("transferAmount")
    private String transferAmount;
    @JsonProperty("transferFee")
    private String transferFee;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("disCountCode")
    private String disCountCode;
    @JsonProperty("addInfoList")
    private List<AddInfo> addInfoList;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddInfo {
        @JsonProperty("name")
        private String name;
        @JsonProperty("type")
        private String type;
        @JsonProperty("value")
        private String value;
    }

    @Override
    public String toString() {
        return "[BAAS] Chi hộ request:{" +
                "serviceType='" + serviceType + '\'' +
                ", customerType='" + customerType + '\'' +
                ", customerLevel='" + customerLevel + '\'' +
                ", debitType='" + debitType + '\'' +
                ", debitResourceNumber='" + debitResourceNumber + '\'' +
                ", debitName='" + debitName + '\'' +
                ", creditType='" + creditType + '\'' +
                ", creditResourceNumber='" + creditResourceNumber + '\'' +
                ", creditName='" + creditName + '\'' +
                ", transferType='" + transferType + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", transferAmount='" + transferAmount + '\'' +
                ", transferFee='" + transferFee + '\'' +
                ", remark='" + remark + '\'' +
                ", disCountCode='" + disCountCode + '\'' +
                ", addInfoList=" + addInfoList +
                '}';
    }
}
