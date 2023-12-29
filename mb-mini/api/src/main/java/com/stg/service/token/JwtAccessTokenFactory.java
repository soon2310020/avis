package com.stg.service.token;

import com.stg.config.AccessTokenFactory;
import com.stg.config.KeyPairProvider;
import com.stg.entity.customer.RefreshToken;
import com.stg.errors.RefreshTokenNotFoundException;
import com.stg.repository.RefreshTokenRepository;
import com.stg.service.dto.baas.OauthToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static com.stg.utils.Constants.BEARER;

@Component
@Transactional
@RequiredArgsConstructor
public class JwtAccessTokenFactory implements AccessTokenFactory {

    private static final String KID = "kid";
    private static final ZoneOffset UTC = ZoneOffset.UTC;
    public static final String COLON = ":";

    private final KeyPairProvider keyPairProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.oauth.keyStoreAlias}")
    private String keyPairAlias;

    @Override
    public OauthToken createAccessToken(String token, String mbId) {

        LocalDateTime now = LocalDateTime.now();
        Date issuedAt = Date.from(now.toInstant(UTC));
        Date expiresAt = Date.from(
                now.plusSeconds(300)
                        .toInstant(UTC)
        );
        Date refreshTokenExpiresAt = Date.from(
                now.plusSeconds(3600)
                        .toInstant(UTC)
        );

        String accessTokenId = createTokenId();
        String subject = token + COLON + mbId;
        String accessToken = generateAccessToken(accessTokenId, subject, issuedAt, expiresAt);
        String refreshToken = DigestUtils.sha256Hex(createTokenId());

        refreshTokenRepository.save(new RefreshToken()
                .setToken(refreshToken)
                .setMbId(mbId)
                .setIssuedAt(issuedAt)
                .setExpiresAt(refreshTokenExpiresAt)
                .addAccessTokenId(new RefreshToken.AccessTokenId()
                        .setAti(accessTokenId)
                        .setExpiresAt(expiresAt)));
        return new OauthToken()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setTokenType(BEARER)
                .setExpiresIn((expiresAt.getTime() - System.currentTimeMillis()) / 1000L);
    }

    @Override
    public OauthToken genAccessTokenFromRefreshToken(String refreshToken) {
        LocalDateTime now = LocalDateTime.now();
        Date issuedAt = Date.from(now.toInstant(UTC));
        Date expiresAt = Date.from(
                now.plusSeconds(300)
                        .toInstant(UTC)
        );
        Date refreshTokenExpiresAt = Date.from(
                now.plusSeconds(3600)
                        .toInstant(UTC)
        );

        RefreshToken storedRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Could not find refresh token=" + refreshToken));
        String accessTokenId = createTokenId();
        String subject = storedRefreshToken.getToken() + COLON + storedRefreshToken.getMbId();
        String accessToken = generateAccessToken(accessTokenId, subject, issuedAt, expiresAt);
        storedRefreshToken.addAccessTokenId(new RefreshToken.AccessTokenId()
                .setAti(accessTokenId)
                .setExpiresAt(expiresAt));
        storedRefreshToken.setExpiresAt(refreshTokenExpiresAt);
        return new OauthToken()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setTokenType(BEARER)
                .setExpiresIn(expiresAt.getTime() - System.currentTimeMillis() / 1000L);
    }

    private static String createTokenId() {
        return UUID.randomUUID().toString();
    }

    private String generateAccessToken(String id, String subject,
                                       Date issuedAt, Date expiresAt) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.RS256, keyPairProvider.getKeyPair(keyPairAlias).getPrivate())
                .setHeaderParam(KID, keyPairAlias)
                .setId(id)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .compact();
    }
}
