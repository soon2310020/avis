package vn.com.twendie.avis.security.core.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.core.payload.Token;
import vn.com.twendie.avis.security.core.payload.UserToken;

public interface TokenProvider {

	Authentication getAuthentication(String token);

	UserToken generateToken(Authentication authentication);

	Token refreshToken(String refreshToken, String accessToken);

	void clearTokenOnSystem(String token);

	default OAuth2Authentication buildAuthentication(String username) {
		HashMap<String, String> authorizationParameters = new HashMap<>();
		authorizationParameters.put("scope", "read");
		authorizationParameters.put("username", "user");
		authorizationParameters.put("client_id", "client_id");
		authorizationParameters.put("grant", "password");

		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		Set<String> responseType = new HashSet<>();
		responseType.add("password");

		Set<String> scopes = new HashSet<>();
		scopes.add("read");
		scopes.add("write");

		OAuth2Request authorizationRequest = new OAuth2Request(authorizationParameters, "Client_Id", authorities, true,
				scopes, null, "", responseType, null);

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
				null, authorities);

		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest,
				authenticationToken);
		authentication.setAuthenticated(true);

		return authentication;
	}

	default UserToken toUserToken(OAuth2AccessToken oAuth2AccessToken, UserPrincipal userPrincipal) {
		if (oAuth2AccessToken == null || userPrincipal == null) {
			return null;
		}
		List<String> listRole = new ArrayList<>();
		if (userPrincipal.getAuthorities() != null) {
			listRole.addAll(userPrincipal.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.collect(Collectors.toList()));
		}

		String departmentName = null;
		String name = null;
		if(userPrincipal.getUser() != null && userPrincipal.getUser().getMemberCustomer() != null){
			departmentName = userPrincipal.getUser().getMemberCustomer().getDepartment();
			name = userPrincipal.getUser().getMemberCustomer().getName();
		}


		return UserToken.builder()
				.accessToken(oAuth2AccessToken.getValue())
				.refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
				.expiresIn(oAuth2AccessToken.getExpiresIn())
				.id(userPrincipal.getId())
				.username(userPrincipal.getUsername())
				.name(name != null ? name : userPrincipal.getName())
				.email(userPrincipal.getEmail())
				.avatar(userPrincipal.getAvatar())
				.firstTimeLogin(userPrincipal.getLoginTimes() == 0)
				.listRole(listRole)
				.departmentName(departmentName)
				.build();
	}

	default Token toToken(OAuth2AccessToken oAuth2AccessToken) {
		return Token.builder()
				.accessToken(oAuth2AccessToken.getValue())
				.refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
				.expiresIn(oAuth2AccessToken.getExpiresIn())
				.build();
	}
}
