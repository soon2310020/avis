package com.stg.service.dto.baas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class TransferNonOtpRespDto {
    @JsonProperty("clientMessageId")
    private String clientMessageId;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("errorDesc")
    private List<String> errorDesc;
    @JsonProperty("data")
    private DataResp data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataResp {
        @JsonProperty("transStatus")
        private String transStatus;
        @JsonProperty("ftNumber")
        private String ftNumber;
        @JsonProperty("soaErrorCode")
        private String soaErrorCode;
        @JsonProperty("soaErrorDesc")
        private String soaErrorDesc;
        @JsonProperty("rrn")
        private String rrn;
    }

    @Override
    public String toString() {
        return "[BAAS] Chi hộ response: {" +
                "clientMessageId='" + clientMessageId + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorDesc=" + errorDesc +
                ", data=" + data +
                '}';
    }
}
