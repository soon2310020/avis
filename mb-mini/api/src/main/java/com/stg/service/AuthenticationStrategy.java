package com.stg.service;

import com.stg.service.dto.oauth.AuthenticationRequestDto;
import com.stg.service.dto.oauth.AuthenticationResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;

public interface AuthenticationStrategy<A extends AuthenticationRequestDto> {

    AuthenticationResponseDto authenticate(A authenticationRequest);

    @SuppressWarnings("unchecked")
    default Class<A> supports() {
        return (Class<A>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @AllArgsConstructor
    @Getter
    enum LoginMethod {
        PASSWORD("password"), SOCIAL("social"), SSO("sso");

        private String val;
    }

    @AllArgsConstructor
    @Getter
    enum Scope {
        USER("user");

        private String val;
    }
}
