package vn.com.twendie.avis.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.rest.exception.ApiCodeException;
import vn.com.twendie.avis.security.core.filter.TokenProvider;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.core.payload.Token;
import vn.com.twendie.avis.security.core.payload.UserToken;
import vn.com.twendie.avis.security.jdbc.repository.MemberCustomerAuthenRepo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProviderImpl implements TokenProvider {

    // Secret JWT that only is known by server side
    @Value("${authen.jwt.secretKey}")
    private String jwtSecret;

    // expired time of token
    @Value("${authen.jwt.expiredTime}")
    private Duration jwtExpiration;

    private final UserDetailsService userDetailsService;

    private final MemberCustomerAuthenRepo memberCustomerRepo;

	public JwtTokenProviderImpl(UserDetailsService userDetailsService, MemberCustomerAuthenRepo memberCustomerRepo) {
		this.userDetailsService = userDetailsService;
		this.memberCustomerRepo = memberCustomerRepo;
	}

    @Override
    public UserToken generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if(userPrincipal.getUser() != null && userPrincipal.getUser().getMemberCustomer() == null){
            userPrincipal.getUser().setMemberCustomer(memberCustomerRepo.findFirstByUser(userPrincipal.getUser()));
        }
        return generateToken(userPrincipal);
    }

    @Override
    public Token refreshToken(String refreshToken, String accessToken) {
        UserToken userToken = generateToken(getAuthentication(refreshToken));
        return Token.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .expiresIn(userToken.getExpiresIn())
                .build();
    }

    private UserToken generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration.toMillis());

        String jwt = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();

        List<String> listRole = new ArrayList<>();
        if (userPrincipal.getAuthorities() != null) {
            listRole.addAll(userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        return UserToken.builder()
                .accessToken(jwt)
                .refreshToken(jwt)
                .expiresIn((int) jwtExpiration.getSeconds())
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .name(userPrincipal.getName())
                .email(userPrincipal.getEmail())
                .avatar(userPrincipal.getAvatar())
                .firstTimeLogin(userPrincipal.getLoginTimes() == 0)
                .listRole(listRole)
                .build();
    }

    @Override
    public Authentication getAuthentication(String token) {
        try {
            if (!StringUtils.isBlank(token) && validateToken(token)) {
                String username = getUsernameFromJWT(token);
                UserPrincipal userDetail = (UserPrincipal) userDetailsService.loadUserByUsername(username);
                return new UsernamePasswordAuthenticationToken(userDetail, userDetail.getPassword(),
                        userDetail.getAuthorities());
            } else {
                log.warn("(Cant not authentication from token {} because null or empty", token);
            }
        } catch (Exception e) {
            throw new ApiCodeException("Error when get authenticate for token " + token, e);
        }
        return null;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    @Override
    public void clearTokenOnSystem(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().clear();
        } catch (Exception ignored) {
        }
    }
}
