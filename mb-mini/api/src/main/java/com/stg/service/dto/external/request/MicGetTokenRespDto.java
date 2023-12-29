package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MicGetTokenRespDto {
    private String code;
    private String message;
    private GetTokenData data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class GetTokenData {
        private String access_token;
        private Integer expires_in;
        private String token_type;
        private String roles;
        private String merchant;
    }

}
