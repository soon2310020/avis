package saleson.common.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.common.enumeration.AuthAction;
import saleson.common.log.LogService;
import saleson.model.User;

@Slf4j
public class OpAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	private String defaultFailureUrl;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Autowired
	private LogService logService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	public OpAuthenticationFailureHandler() {
	}

	public OpAuthenticationFailureHandler(String defaultFailureUrl) {
		setDefaultFailureUrl(defaultFailureUrl);
	}

	public void setDefaultFailureUrl(String defaultFailureUrl) {
		super.setDefaultFailureUrl(defaultFailureUrl);
		this.defaultFailureUrl = defaultFailureUrl;
	}

	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
		String username = ThreadUtils.getProp("username", () -> request.getParameter("username"));

		if (this.defaultFailureUrl == null || isUseForward() || ObjectUtils.isEmpty(username)) {
			super.onAuthenticationFailure(request, response, e);
			return;
		}

		saveException(request, e);

		String failureUrl = defaultFailureUrl;
		if (e instanceof BadCredentialsException) {
			User user = userRepository.findByLoginIdAndDeletedIsFalse(username).orElse(null);
			if (user != null) {
				if (user.isEnabled() && !user.isAccountLocked()) {
					if (user.getFailedAttempt() < ConfigUtils.getAccountPasswordMaxFailedAttempts() - 1) {
						userService.increaseFailedAttempts(user);
					} else {
						userService.lock(user);
						e = new LockedException("Your account has been locked due to 3 failed attempts." + " It will be unlocked after 24 hours.");
						userService.sendLockAccountMail(user);
					}
				} else if (user.isAccountLocked()) {
					if (userService.unlockWhenTimeExpired(user)) {
						e = new LockedException("Your account has been unlocked. Please try to login again.");
					}
				}
			}

		} else if (e instanceof CredentialsExpiredException) {
			failureUrl += "=2";

			User user = userRepository.findByLoginIdAndDeletedIsFalse(username).orElse(null);
			userService.lock(user);

			e = new CredentialsExpiredException("Your password has been expired." + "  Please check your email to reset with a new password.");
			userService.sendPwdExpiredMail(user);

		} else if (e instanceof DisabledException) {
			User user = userRepository.findByLoginIdAndDeletedIsFalse(username).orElse(null);
			if (user != null //
					&& ValueUtils.toBoolean(user.getRequested(), false)//
					&& !ObjectUtils.isEmpty(user.getSsoId()) //
					&& "<PASSWORD>".equals(user.getPassword()) //
			) {
				failureUrl += "=3";
			}
		}

		if (e instanceof LockedException) {
			failureUrl += "=1";
		}

		logService.saveAuthenticationLog(username, AuthAction.FAILURE, e.getMessage());

		log.debug("Redirecting to " + failureUrl);
		redirectStrategy.sendRedirect(request, response, failureUrl);
	}
}
