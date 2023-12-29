package vn.com.twendie.avis.security.config.auto;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.Assert;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.core.payload.Token;
import vn.com.twendie.avis.security.core.payload.UserToken;
import vn.com.twendie.avis.security.jdbc.provider.AbstractTokenProvider;

/**
 * This class to define default token provider storage token use database
 * 
 * @author trungnt9
 *
 */
public class DefaultTokenProvider extends AbstractTokenProvider implements InitializingBean {

	private UserDetailsService userDetailServices;

	private DefaultTokenServices defaultTokenServices;

	public DefaultTokenProvider(UserDetailsService userDetailServices, DefaultTokenServices defaultTokenServices,
			TokenStore tokenStore) {
		super(tokenStore);
		this.userDetailServices = userDetailServices;
		this.defaultTokenServices = defaultTokenServices;
	}

	@Override
	public UserToken generateToken(Authentication authentication) {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		OAuth2AccessToken oauth2AccessToken = buildAccessToken(buildAuthentication(userPrincipal.getUsername()));
		return toUserToken(oauth2AccessToken, userPrincipal);
	}

	@Override
	public Token refreshToken(String refreshToken, String accessToken) {
		OAuth2AccessToken oAuth2AccessToken = defaultTokenServices.refreshAccessToken(refreshToken, null);
		return toToken(oAuth2AccessToken);
	}

	private OAuth2AccessToken buildAccessToken(OAuth2Authentication oauth2Authen) {
		return defaultTokenServices.createAccessToken(oauth2Authen);
	}

	@Override
	protected UserPrincipal loadByUsernameOrEmail(String usernameOrEmail) {
		return (UserPrincipal) userDetailServices.loadUserByUsername(usernameOrEmail);
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(tokenStore, "tokenStore must be set");
		Assert.notNull(userDetailServices, "userDetailServices must be set");
		Assert.notNull(defaultTokenServices, "defaultTokenServices must be set");
	}

	@Override
	public void clearTokenOnSystem(String token) {
		defaultTokenServices.revokeToken(token);
	}

}
