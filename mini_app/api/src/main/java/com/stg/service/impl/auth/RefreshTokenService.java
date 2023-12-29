package com.stg.service.impl.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.config.security.auth.AccessToken;
import com.stg.config.security.auth.JwtAuthenticationProvider;
import com.stg.config.security.auth.JwtClaims;
import com.stg.config.security.auth.UserCredential;
import com.stg.entity.RefreshToken;
import com.stg.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

	@Value("${jwt.refreshTokenValiditySeconds:43200}") // 12 hours
	private int refreshTokenValiditySeconds;

	@Autowired
	private RefreshTokenRepository repository;

	@Autowired
	private JwtAuthenticationProvider jwtAuthenticationProvider;
	
	@Autowired
	private ObjectMapper mapper;
	
	
	@Transactional
    public RefreshToken createRefreshToken(String username, String rmCode, String icCode, String branchCode, String branchName,
            String fullName, String phoneNumber, String email) {
        UserCredential credential = new UserCredential(username, rmCode, icCode, branchCode, branchName, fullName, phoneNumber,
                email);
		return createRefreshToken(username, credential);
	}

	@Transactional
    public RefreshToken createRefreshToken(String username, UserCredential credential) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        
        String credentialJson = null;
        try {
            credentialJson = mapper.writeValueAsString(credential);
        } catch (JsonProcessingException e) {
        }
        
        token.setCredentials(credentialJson);
        
        token.setExpiredDate(Instant.ofEpochMilli(System.currentTimeMillis() + (refreshTokenValiditySeconds * 1000L)));
        repository.save(token);
        return token;
    }
	
	public RefreshToken getRefreshToken(String token) {
		Optional<RefreshToken> refreshToken = repository.findById(token);
		if (refreshToken.isPresent() && refreshToken.get().getExpiredDate().isAfter(Instant.now())) {
			return refreshToken.get();
		}
		return null;
	}

	@Transactional
	public AccessToken refreshToken(String token) {
		RefreshToken refreshToken = getRefreshToken(token);
		if (token == null) {
			throw new AuthenticationServiceException("Unauthorized");
		}
		refreshToken.setExpiredDate(Instant.ofEpochMilli(System.currentTimeMillis() + (300 * 1000)));
		repository.save(refreshToken);
		
		UserCredential credential = null;
		JwtClaims claims = new JwtClaims();
        try {
            credential = mapper.readValue(refreshToken.getCredentials(), UserCredential.class);
            claims.setRmCode(credential.getRmCode());
            claims.setIcCode(credential.getIcCode());
            claims.setBranchCode(credential.getBranchCode());
            claims.setBranchName(credential.getBranchName());
            claims.setFullName(credential.getFullName());
            claims.setPhoneNumber(credential.getPhoneNumber());
            claims.setEmail(credential.getEmail());
        } catch (Exception e) {
        }
		
		refreshToken = createRefreshToken(refreshToken.getUsername(), credential);
		
		User user = new User(refreshToken.getUsername(), "", List.of());
		
		AccessToken accessToken = jwtAuthenticationProvider.getToken(new UsernamePasswordAuthenticationToken(user, null, null), claims);
		accessToken.setRefreshToken(refreshToken.getToken());
		return accessToken;
	}

}
