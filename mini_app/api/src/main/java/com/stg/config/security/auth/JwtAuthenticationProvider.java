package com.stg.config.security.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationProvider {

    @Autowired
    private KeyPairProvider keyPairProvider;

    @Value("${jwt.accessTokenValiditySeconds:3600}") // 1 hour
    private int accessTokenValiditySeconds;

    @Value("${security.internal.x-auth-token}")
    private String xAuthTokenInternalServer;

    public AbstractAuthenticationToken getAuthentication(String jwt) {
        Claims claims = Jwts.parser().setSigningKey(keyPairProvider.getPublicKey()).parseClaimsJws(jwt).getBody();
        
        String username = claims.getSubject();
        String rmCode = (String) claims.get(JwtClaims.RM_CODE);
        String icCode = (String) claims.get(JwtClaims.IC_CODE);
        String branhCode = (String) claims.get(JwtClaims.BRANCH_CODE);
        String branhName = (String) claims.get(JwtClaims.BRANCH_NAME);
        String fullName = (String) claims.get(JwtClaims.FULL_NAME);
        String phoneNumber = (String) claims.get(JwtClaims.PHONE_NUMBER);
        String email = (String) claims.get(JwtClaims.EMAIL);
        
        UserCredential credential = new UserCredential(username, rmCode, icCode, branhCode, branhName, fullName, phoneNumber,
                email);
        
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_API"));
        
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", authorities), credential, authorities);
    }

    public AccessToken getToken(Authentication auth) {
    	return getToken(auth, null);
    }
    
    public AccessToken getToken(Authentication auth, JwtClaims claims) {
        User user = (User) auth.getPrincipal();

        Date expiredDate = new Date(System.currentTimeMillis() + (accessTokenValiditySeconds * 1000L));

        String token = Jwts.builder().setClaims(claims).setSubject(user.getUsername()).setIssuedAt(new Date())
                .setExpiration(expiredDate).signWith(SignatureAlgorithm.RS256, keyPairProvider.getPrivateKey()).compact();

        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken(token);
        accessToken.setExpiredTime(expiredDate.getTime());
        accessToken.setUsername(user.getUsername());

        return accessToken;
    }

    /** */
    public AbstractAuthenticationToken getAuthInternalServer(String clientId, String xAuthToken) {
        if (StringUtils.hasText(clientId) && xAuthTokenInternalServer.equals(xAuthToken)) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_API"), new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new UsernamePasswordAuthenticationToken(new User(clientId, xAuthToken, authorities), null, authorities);
        }
        return null;
    }
}
