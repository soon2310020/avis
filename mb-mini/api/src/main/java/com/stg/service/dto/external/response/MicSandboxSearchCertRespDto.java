package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicSandboxSearchCertRespDto extends MicSandboxResp {

    @JsonProperty("data")
    private SearchInsuranceCertData data;

}
