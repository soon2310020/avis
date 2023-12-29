package com.stg.service.impl;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.user.FeatureUser;
import com.stg.entity.user.User;
import com.stg.repository.FeatureUserRepository;
import com.stg.repository.UserRepository;
import com.stg.service.AuthenticationService;
import com.stg.service.dto.oauth.AuthenticatedUser;
import com.stg.service.dto.oauth.AuthenticationResponseDto;
import com.stg.service.dto.oauth.EmailAndPasswordAuthenticationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;
    private final AuthenticationService authenticationService;
    private final FeatureUserRepository featureUserRepository;

    public UserDetailsServiceImpl(UserRepository repository,
                                  AuthenticationService authenticationService,
                                  FeatureUserRepository featureUserRepository) {
        this.repository = repository;
        this.authenticationService = authenticationService;
        this.featureUserRepository = featureUserRepository;
    }

    public UserDetails loadUser(String email, String password) {
        EmailAndPasswordAuthenticationDto authenticationDto = new EmailAndPasswordAuthenticationDto()
                .setEmail(email)
                .setPassword(password);
        AuthenticationResponseDto loginUser = authenticationService.authenticate(authenticationDto);

        return new CustomUserDetails(((AuthenticatedUser) loginUser.getUser()).getId().toString(),
                email,
                ((AuthenticatedUser) loginUser.getUser()).getRole(), loginUser.getFeatures());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        List<User> users = repository.findUsersByEmailIn(Collections.singleton(email));
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username=" + email);
        }
        FeatureUser featureUser = featureUserRepository.findByUserId(users.get(0).getId());
        return new CustomUserDetails(users.get(0).getId().toString(), email, users.get(0).getRole().name(),
                featureUser == null ? new ArrayList<>() : featureUser.getFeatureAsList());
    }
}
