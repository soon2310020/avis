package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbalCreateOrderRespDto {

    @JsonProperty(value = "id")
    private String id;
    @JsonProperty(value = "merchant")
    private Merchant merchant;
    @JsonProperty(value = "cif")
    private String cif;
    @JsonProperty(value = "amount")
    private Long amount;
    @JsonProperty(value = "description")
    private String description;
    @JsonProperty(value = "type")
    private Type type;
    @JsonProperty(value = "successMessage")
    private String successMessage;
    @JsonProperty(value = "metadata")
    private String metadata;
    @JsonProperty(value = "createdTime")
    @Schema(description = "createdTime", required = true, example = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdTime;
    @JsonProperty(value = "paidTime")
    @Schema(description = "paidTime", required = true, example = "yyyy-MM-dd'T'HH:mm:ss")
    private Date paidTime;
    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "fundingSource")
    private String fundingSource;
    @JsonProperty(value = "cardType")
    private String cardType;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Merchant {
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Type {
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
        @JsonProperty("allowCard")
        private Boolean allowCard;
    }

    @Override
    public String toString() {
        return "MbalCreateOrderRespDto{" +
                "id='" + id + '\'' +
                ", merchant=" + merchant +
                ", cif='" + cif + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", successMessage='" + successMessage + '\'' +
                ", metadata='" + metadata + '\'' +
                ", createdTime=" + createdTime +
                ", paidTime=" + paidTime +
                ", status='" + status + '\'' +
                ", fundingSource='" + fundingSource + '\'' +
                ", cardType='" + cardType + '\'' +
                '}';
    }
}
