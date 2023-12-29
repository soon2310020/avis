package com.stg.config.auth.impl;

import com.stg.config.auth.JwtAuthenticationProvider;
import com.stg.config.auth.PublicKeyProvider;
import com.stg.entity.user.CustomerIdentifier;
import com.stg.errors.ApplicationException;
import com.stg.errors.AuthenticateFailException;
import com.stg.errors.TokenHasExpiredException;
import com.stg.repository.BlackListTokenRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.List;

import static org.springframework.util.StringUtils.isEmpty;

@RequiredArgsConstructor
@Slf4j
@Component
public class LegacyJwtAuthenticationProvider implements JwtAuthenticationProvider {
    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    private static final String CLAIM_USER_ROLE = "role";
    private static final String CLAIM_KEY_USERNAME = "user_name";
    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_JTI = "jti";
    private static final String CLAIM_KEY_ATI = "ati";
    private static final String SCOPE = "scope";

    private final PublicKeyProvider publicKeyProvider;
    private final BlackListTokenRepository blackListTokenRepository;

    @Override
    public AbstractAuthenticationToken get(String jwt) {
        Claims claims = getClaimsFromToken(jwt);
        String username = claims.get(CLAIM_KEY_USERNAME, String.class);
        String role = claims.get(CLAIM_USER_ROLE, String.class);
        String userId = claims.get(CLAIM_KEY_USER_ID, String.class);
        String jti = claims.get(CLAIM_KEY_JTI, String.class);
        List<String> scopes = (List<String>) claims.get(SCOPE);
        String ati = claims.get(CLAIM_KEY_ATI, String.class);
        if (ati != null) {
            throw new AuthenticateFailException("This is refresh_token, not access_token");
        }
        if (scopes != null) {
            if (isEmpty(username)) {
                throw new IllegalArgumentException("username claim must be present");
            }

            if (blackListTokenRepository.findById(jti).isPresent()) {
                throw new TokenHasExpiredException("Token has expired");
            }

            CustomUserDetails userDetails = new CustomUserDetails()
                    .setUserId(userId)
                    .setEmail(username)
                    .setRole(role);

            return new UsernamePasswordAuthenticationToken(
                    userDetails, null, AuthorityUtils.createAuthorityList(role));
        }
        CustomerIdentifier identifier = CustomerIdentifier.of(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(
                identifier, null, AuthorityUtils.createAuthorityList(ROLE_CUSTOMER));

    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(publicKeyProvider.getPublicKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new AuthenticateFailException("Wrong token");
        }
    }

}
