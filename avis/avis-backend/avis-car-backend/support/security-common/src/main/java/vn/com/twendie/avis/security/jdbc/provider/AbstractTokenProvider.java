package vn.com.twendie.avis.security.jdbc.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import vn.com.twendie.avis.api.rest.exception.UnauthorizedException;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.core.filter.TokenProvider;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import java.util.Objects;

/**
 * @author trungnt9
 *
 */
@Slf4j
public abstract class AbstractTokenProvider implements TokenProvider {

	protected TokenStore tokenStore;

	public AbstractTokenProvider(TokenStore tokenStore) {
		this.tokenStore = tokenStore;
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

		String usernameOrEmail = authentication.getName();
		UserPrincipal userDetail = loadByUsernameOrEmail(usernameOrEmail);
		return new UsernamePasswordAuthenticationToken(userDetail, userDetail.getPassword(),
				userDetail.getAuthorities());

	}

	protected abstract UserPrincipal loadByUsernameOrEmail(String usernameOrEmail);
}
