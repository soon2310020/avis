package com.stg.config.security.auth;

import com.stg.service3rd.crm.dto.resp.RmUserDetailDto;
import com.stg.service3rd.mbal.dto.resp.ICDataResp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessToken {

    private String accessToken;
    private String refreshToken;
    private String username;
    private Long expiredTime;

    private ICDataResp icInfo;
    private RmUserDetailDto rmInfo;
}
