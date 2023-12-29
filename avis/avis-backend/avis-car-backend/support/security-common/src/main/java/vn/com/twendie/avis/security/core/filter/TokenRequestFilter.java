package vn.com.twendie.avis.security.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.twendie.avis.api.rest.model.ApiError;
import vn.com.twendie.avis.api.rest.model.ApiResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class TokenRequestFilter extends OncePerRequestFilter {

	private final TokenProvider tokenProvider;

	private final ObjectMapper objectMapper;

	public TokenRequestFilter(TokenProvider tokenProvider,
							  ObjectMapper objectMapper) {
		this.tokenProvider = tokenProvider;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
									@NonNull HttpServletResponse response,
									@NonNull FilterChain chain)
			throws ServletException, IOException {
		log.info("(doFilter)START");
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null) {
				String requestTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
				if (StringUtils.isBlank(requestTokenHeader)) {
					requestTokenHeader = getCookieToken(request);
				}
				authentication = tokenProvider.getAuthentication(requestTokenHeader);
				if (authentication != null) {
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
				log.info("(doFilter) FINISH not authenticated");
			} else {
				log.info("(doFilter) FINISH authenticated");
			}
		} catch (Exception e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			PrintWriter writer = response.getWriter();
			writer.println(objectMapper.writeValueAsString(
					ApiResponse.error(ApiError.buildApiError(e, HttpStatus.UNAUTHORIZED))
			));
			writer.close();
			return;
		}
		chain.doFilter(request, response);
	}

	private String getCookieToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(HttpHeaders.AUTHORIZATION)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

}
