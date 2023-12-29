package com.stg.service.dto.mic;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MicGetTokenReqDto {

    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
}
