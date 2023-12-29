package com.stg.service.dto.insurance;

import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import com.stg.service.dto.external.response.MicInsuranceResultRespDto;
import com.stg.service.dto.external.responseFlexible.MicBenefitRespDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdditionalInsuredDTO {

    private InsuredDTO assured;
//    private AdditionalProductDTOS additionalProduct;
    private List<AdditionalProductDTO> additionalProducts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class AdditionalProductDTOS {

        private List<AdditionalProductDTO> riders;

        private MicAdditionalProduct micRequest;

        private MicInsuranceResultRespDto micResult;

        private MicBenefitRespDto micBenefit;

    }

}
