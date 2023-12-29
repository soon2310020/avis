package saleson.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import saleson.api.auth.AuthService;
import saleson.common.enumeration.RoleUserData;
import saleson.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by rajeevkumarsingh on 19/08/17.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private OpUserDetailsService opUserDetailsService;
	@Autowired
	AuthService authService;

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			String jwt = getJwtFromRequest(request);

			if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
				Long userId = tokenProvider.getUserIdFromJWT(jwt);

				/*
				    Note that you could also encode the user's username and roles inside JWT claims
				    and create the UserDetails object by parsing those claims from the JWT.
				    That would avoid the following database hit. It's completely up to you.
				 */
				UserDetails userDetails = opUserDetailsService.loadUserById(userId);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				//authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				if (jwt != null) {
					response.setStatus(401);
					return;
				}
			}
			//authentication role
			if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null) {
				boolean isRest = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
						.anyMatch(a -> RoleUserData.ROLE_REST_USER.name().equals(a.getAuthority()));
				if (isRest && request.getServletPath().startsWith("/api") && !request.getServletPath().startsWith("/api/integration/") && !request.getServletPath().startsWith("/api/auth/")) {
					response.setStatus(403);
					response.getWriter().write("Rest user cannot access web");
					response.getWriter().flush();
					response.getWriter().close();
					return;
				}
			}
		} catch (Exception ex) {
			logger.error("Could not set user authentication in security context", ex);
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}
}
