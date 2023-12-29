package saleson.common.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginIn;
import com.emoldino.api.common.resource.composite.manpag.service.login.ManPagLoginService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;

import lombok.extern.slf4j.Slf4j;
import saleson.api.user.UserRepository;
import saleson.model.User;

@Slf4j
public class SAMLAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	@Autowired
	private UserRepository userRepository;

	@Transactional
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		User user = userRepository.findById(principal.getId()).orElse(null);

		log.debug("[SSO-SUCCESS] userPrincipal: {}", principal);

		if (user != null) {
			user.setLoginId(principal.getEmail());
			user.setEmail(principal.getEmail());
			user.setName(principal.getName());
			user.setLastLogin(DateUtils2.newInstant());
			userRepository.save(user);
		}

		HttpSession session = request.getSession(false);
		BeanUtils.get(ManPagLoginService.class).login(ManPagLoginIn.builder().sessionId(session == null ? null : session.getId()).username(principal.getUsername()).build());

		this.setDefaultTargetUrl("/");
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
