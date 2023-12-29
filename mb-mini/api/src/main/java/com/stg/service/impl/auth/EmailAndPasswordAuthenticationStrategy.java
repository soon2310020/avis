package com.stg.service.impl.auth;

import com.stg.entity.credential.EmailAndPasswordCredential;
import com.stg.entity.user.FeatureUser;
import com.stg.entity.user.User;
import com.stg.errors.CredentialNotMatchingException;
import com.stg.repository.CredentialRepository;
import com.stg.repository.FeatureUserRepository;
import com.stg.service.AuthenticationStrategy;
import com.stg.service.dto.oauth.AuthenticatedUser;
import com.stg.service.dto.oauth.AuthenticationResponseDto;
import com.stg.service.dto.oauth.EmailAndPasswordAuthenticationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;

import static com.stg.service.impl.auth.AuthenticationUtils.verifyCredentialIsNonExpired;
import static com.stg.service.impl.auth.AuthenticationUtils.verifyUserIsNotLocked;

@Slf4j
@RequiredArgsConstructor
@Component
public class EmailAndPasswordAuthenticationStrategy implements AuthenticationStrategy<EmailAndPasswordAuthenticationDto> {

    private static final int MAX_FAILED_MATCHES = 5;

    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;
    private final FeatureUserRepository featureUserRepository;

    @Override
    @Transactional(dontRollbackOn = CredentialNotMatchingException.class)
    public AuthenticationResponseDto authenticate(EmailAndPasswordAuthenticationDto authenticationRequest) {

        log.debug("Retrieved email and password with email={}",
                authenticationRequest.getEmail());

        Optional<EmailAndPasswordCredential> byEmailAndEnabledFalse = credentialRepository.findByEmailAndEnabledFalse(authenticationRequest.getEmail());
        if (byEmailAndEnabledFalse.isPresent()) {
            AuthenticationUtils.createUserInactiveException(authenticationRequest.getEmail());
        }

        EmailAndPasswordCredential credential = credentialRepository
                .findByEmailAndEnabledTrue(authenticationRequest.getEmail())
                .orElseThrow(() -> AuthenticationUtils.createUserNotFoundException(authenticationRequest.getEmail()));

        User user = credential.getUser();

        verifyUserIsNotLocked(user);
        verifyCredentialIsNonExpired(credential);

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), credential.getPassword())) {
            log.error("Authentication failed for user=[{} {}] with userId={}",
                    user.getFirstName(), user.getLastName(), user.getId());

            throw new CredentialNotMatchingException("Password not matching");
        }
        FeatureUser featureUser = featureUserRepository.findByUserId(user.getId());

        return new AuthenticationResponseDto()
                .setScope(Scope.USER.getVal())
                .setLoginMethod(LoginMethod.PASSWORD.getVal())
                .setUser(new AuthenticatedUser()
                        .setId(user.getId())
                        .setEmail(user.getEmail())
                        .setFirstName(user.getFirstName())
                        .setLastName(user.getLastName())
                        .setRole(user.getRole().name().toLowerCase()))
                .setFeatures(featureUser == null ? new ArrayList<>() : featureUser.getFeatureAsList());
    }

    @Override
    public Class<EmailAndPasswordAuthenticationDto> supports() {
        return EmailAndPasswordAuthenticationDto.class;
    }

}
