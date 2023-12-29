package com.stg.service.dto.external.responseFlexible;

import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import com.stg.service.dto.external.response.MicInsuranceResultRespDto;
import com.stg.service.dto.external.response.MicSandboxFeeCareRespDto;
import com.stg.utils.FlexibleCommon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Valid
public class QuoteAssuredOutput {

    private List<FlexibleCommon.Rider> riders;

    private MicAdditionalProduct micRequest;

    private MicSandboxFeeCareRespDto micResult;

    private MicBenefitRespDto micBenefit;

}
