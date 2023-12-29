package com.stg.service.impl.auth;

import com.stg.service.AuthenticationService;
import com.stg.service.AuthenticationStrategy;
import com.stg.service.dto.oauth.AuthenticationRequestDto;
import com.stg.service.dto.oauth.AuthenticationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DefaultAuthenticationService implements AuthenticationService {


    private final List<AuthenticationStrategy> strategies;

    private Map<Class<? extends AuthenticationRequestDto>, AuthenticationStrategy> strategyMap;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        // get all the strategies -- can only have one for each type
        strategyMap = strategies
                .stream()
                .collect(Collectors.toMap(AuthenticationStrategy::supports, Function.identity(), (a, b) -> a));
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto authenticationDto) {
        return strategyMap.get(authenticationDto.getClass()).authenticate(authenticationDto);
    }
}
