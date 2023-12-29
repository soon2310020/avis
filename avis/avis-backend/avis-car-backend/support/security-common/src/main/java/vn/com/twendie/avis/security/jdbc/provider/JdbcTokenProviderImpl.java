package vn.com.twendie.avis.security.jdbc.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vn.com.twendie.avis.api.rest.exception.UnauthorizedException;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.core.payload.Token;
import vn.com.twendie.avis.security.core.payload.UserToken;
import vn.com.twendie.avis.security.core.service.CustomTokenService;

import java.util.Objects;

/**
 * This class to define token provider storage token use database
 * 
 * @author trungnt9
 *
 */
@Component
@Primary
@Slf4j
public class JdbcTokenProviderImpl extends AbstractTokenProvider implements InitializingBean {

	private final UserDetailsService userDetailServices;

	private final CustomTokenService customTokenService;

	public JdbcTokenProviderImpl(TokenStore tokenStore,
								 @Qualifier("avisUserDetailService") UserDetailsService userDetailServices,
								 CustomTokenService customTokenService) {
		super(tokenStore);
		this.userDetailServices = userDetailServices;
		this.customTokenService = customTokenService;
	}

	@Override
	public Authentication getAuthentication(String token) {

		if (StringUtils.isEmpty(token)) {
			return null;
		}

		OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
		OAuth2Authentication authentication = tokenStore.readAuthentication(token);

		if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
			throw new UnauthorizedException("Token is not valid")
					.code(HttpStatus.UNAUTHORIZED.value())
					.displayMessage(Translator.toLocale("auth.login_require"));
		}

		if (accessToken.isExpired()) {
			throw new UnauthorizedException("Session expired")
					.code(HttpStatus.UNAUTHORIZED.value())
					.displayMessage(Translator.toLocale("auth.session_expired"));
		}

		customTokenService.extendExpiredTime(authentication, (DefaultOAuth2AccessToken) accessToken);

		String usernameOrEmail = authentication.getName();
		UserPrincipal userDetail = loadByUsernameOrEmail(usernameOrEmail);
		return new UsernamePasswordAuthenticationToken(userDetail, userDetail.getPassword(),
				userDetail.getAuthorities());

	}

	@Override
	public UserToken generateToken(Authentication authentication) {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		OAuth2AccessToken oauth2AccessToken = buildAccessToken(buildAuthentication(userPrincipal.getUsername()));
		return toUserToken(oauth2AccessToken, userPrincipal);
	}

	@Override
	public Token refreshToken(String refreshToken, String accessToken) {
		OAuth2AccessToken oauth2AccessToken = customTokenService.refreshAccessToken(refreshToken, accessToken);
		return toToken(oauth2AccessToken);
	}

	private OAuth2AccessToken buildAccessToken(OAuth2Authentication oauth2Authen) {
		return customTokenService.createAccessToken(oauth2Authen);
	}

	@Override
	protected UserPrincipal loadByUsernameOrEmail(String usernameOrEmail) {
		return (UserPrincipal) userDetailServices.loadUserByUsername(usernameOrEmail);
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(tokenStore, "tokenStore must be set");
		Assert.notNull(userDetailServices, "userDetailServices must be set");
		Assert.notNull(customTokenService, "defaultTokenServices must be set");
	}

	@Override
	public void clearTokenOnSystem(String token) {
		customTokenService.revokeToken(token);
	}

}
