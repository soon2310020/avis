package com.stg.service3rd.mbal.dto;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(notes = "Thông tin miniAssuredId")
    private String assuredId;

    @Valid
    @ApiModelProperty(notes = "Thông tin gói MIC cho người được BHBS")
    private MicAdditionalProduct micRequest;

    @Valid
    @ApiModelProperty(notes = "Thông tin gói MBAL cho người được BHBS")
    private MbalAdditionalProductInput mbalAdditionalProductInput;

    public QuoteAssuredInput(String assuredId, MbalAdditionalProductInput mbalAdditionalProductInput) {
        this.assuredId = assuredId;
        this.mbalAdditionalProductInput = mbalAdditionalProductInput;
    }
}
