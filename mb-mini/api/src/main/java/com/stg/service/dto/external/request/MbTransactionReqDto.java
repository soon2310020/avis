package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbTransactionReqDto {
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "sessionId", required = true)
    private String sessionId;
    @Schema(description = "allowCard")
    private boolean allowCard;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "cif", required = true)
    private String cif;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(defaultValue = "0 < amount < 10 tỉ VND", required = true)
    private Long amount;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "description", required = true)
    private String description;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "type", required = true)
    private String type;
    @Schema(description = "successMessage")
    private String successMessage;
    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "metadata-Thông tin phí MIC, MBAL", required = true, example = "")
    private String metadata;

    @JsonProperty("rmCode")
    private String rmCode;

    @JsonProperty("rmName")
    private String rmName;

    @JsonProperty("rmPhone")
    private String rmPhone;

    @JsonProperty("isInstallment")
    private Boolean isInstallment;
}
