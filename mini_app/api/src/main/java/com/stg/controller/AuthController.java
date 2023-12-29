package com.stg.controller;

import com.stg.common.Endpoints;
import com.stg.config.security.auth.AccessToken;
import com.stg.service.CrmAuthenticationService;
import com.stg.service.dto.quotation.CrmToken;
import com.stg.service.impl.auth.RefreshTokenService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@Api(tags = "Authentication APIs")
public class AuthController {
	private final RefreshTokenService refreshTokenService;
    private final CrmAuthenticationService crmAuthenticationService;

    @PostMapping(Endpoints.URL_REFRESH_TOKEN)
    @ResponseStatus(HttpStatus.OK)
    public AccessToken refreshToken(@RequestParam String token) {
        return refreshTokenService.refreshToken(token);
    }


    @PostMapping(Endpoints.URL_CRM_VERIFY)
    @ResponseStatus(HttpStatus.OK)
    public AccessToken crmVerifyToken(@Valid @RequestBody CrmToken crmToken) {
        return crmAuthenticationService.verifyToken(crmToken.getToken());
    }

}
