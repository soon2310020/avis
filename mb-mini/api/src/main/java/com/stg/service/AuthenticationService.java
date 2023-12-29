package com.stg.service;


import com.stg.service.dto.oauth.AuthenticationRequestDto;
import com.stg.service.dto.oauth.AuthenticationResponseDto;

public interface AuthenticationService {
    AuthenticationResponseDto authenticate(AuthenticationRequestDto authenticationDto);
}
