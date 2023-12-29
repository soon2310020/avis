package com.stg.service3rd.mbal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Valid
public class AdditionalAssuredOutput {

    private Integer appQuestionNumber;

    private FlexibleCommon.Assured assured;

    private QuoteAssuredOutput additionalProduct;

}
