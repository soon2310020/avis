package com.stg.service.dto.external.requestFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Valid
public class QuoteAssuredInput {

    @NotNull(message = "Thông tin bắt buộc nhập")
    @Schema(description = "Thông tin miniAssuredId")
    private String assuredId;

    @Valid
    @Schema(description = "Thông tin gói MIC cho người được BHBS")
    private MicAdditionalProduct micRequest;

    @Valid
    @Schema(description = "Thông tin gói MBAL cho người được BHBS")
    private MbalAdditionalProductInput mbalAdditionalProductInput;

}
