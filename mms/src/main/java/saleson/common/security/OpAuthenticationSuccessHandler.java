package saleson.common.security;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginIn;
import com.emoldino.api.common.resource.composite.manpag.service.login.ManPagLoginService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.ThreadUtils;

import saleson.api.user.UserRepository;
import saleson.api.user.UserRepositoryImpl;
import saleson.api.user.UserService;
import saleson.common.config.Const;
import saleson.common.enumeration.AuthAction;
import saleson.common.log.LogService;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

public class OpAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private LogService logService;

	@Autowired
	private UserRepositoryImpl userRepositoryImpl;

	@Autowired
	private LocaleResolver localeResolver;

	@Autowired
	UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Value("${host.url}")
	String host;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

		String username = ThreadUtils.getProp("username", () -> request.getParameter("username"));
		User user = userRepository.findByLoginIdAndDeletedIsFalse(username).orElse(null);

		logService.saveAuthenticationLog(username, AuthAction.SUCCESS, "Login success.");

		userRepositoryImpl.saveLastLogin(username);

		HttpSession session = request.getSession(false);
		BeanUtils.get(ManPagLoginService.class).login(ManPagLoginIn.builder().sessionId(session == null ? null : session.getId()).username(username).build());

		// Locale
		UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
		if (up.getFailedAttempt() > 0) {
			userService.resetFailedAttempts(username);
		}

		if (ConfigUtils.getAccountPasswordExpirationDurationDays() > 1) {
			Instant pwdExpiredAt = up.getPwdExpiredAt();

			if (pwdExpiredAt != null) {
				Long pwdExpirationDate = pwdExpiredAt.toEpochMilli();
				Long reminderDate = System.currentTimeMillis() + 604800000; // In last 7 days

				if (reminderDate >= pwdExpirationDate) {
					userService.sendPwdExpireReminder(user);
				}
			} else {
				SecurityUtils.resetPwdExpiredAt(user);
			}
		}


		String language = up.getLanguage() == null ? "en" : up.getLanguage();
		localeResolver.setLocale(request, response, StringUtils.parseLocale(language));

		RequestCache requestCache = new HttpSessionRequestCache();

		SavedRequest savedRequest = requestCache.getRequest(request, response);

		if (savedRequest == null) {
			super.onAuthenticationSuccess(request, response, authentication);

			return;
		}
		String targetUrlParameter = getTargetUrlParameter();
		if (isAlwaysUseDefaultTargetUrl() //
				|| (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
			requestCache.removeRequest(request, response);
			super.onAuthenticationSuccess(request, response, authentication);

			return;
		}

		clearAuthenticationAttributes(request);

		// Use the DefaultSavedRequest URL
		String targetUrl = savedRequest.getRedirectUrl();
		logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
		if (request.getHeader("User-Agent").contains("Mobi")) {
			String baseUrl = ServletUriComponentsBuilder//
					.fromRequestUri(request)//
					.replacePath(null)//
					.build()//
					.toUriString();
			getRedirectStrategy().sendRedirect(request, response, baseUrl + Const.DOWNLOAD_APP);
		} else {
			getRedirectStrategy().sendRedirect(request, response, targetUrl);
		}
	}
}
