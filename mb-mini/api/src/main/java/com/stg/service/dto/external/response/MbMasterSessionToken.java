package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MbMasterSessionToken {
    @JsonProperty("token")
    private String token;
    @JsonProperty("masterSessionToken")
    private String masterSessionToken;
    @JsonProperty("masterSessionExpiredAt")
    private Long masterSessionExpiredAt;
}
