package com.stg.service3rd.mic.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MicGenerateInsuranceCertRespDto {

    @JsonProperty("Code")
    private String Code;
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("data")
    private GenerateInsuranceCertData data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateInsuranceCertData {
        @JsonProperty("gcn")
        private String gcn;
        @JsonProperty("phi")
        private String phi;
        @JsonProperty("so_id")
        private String so_id;
        @JsonProperty("file")
        private String file;
    }

}
