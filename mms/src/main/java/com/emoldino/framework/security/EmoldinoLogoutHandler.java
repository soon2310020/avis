package com.emoldino.framework.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLogoutIn;
import com.emoldino.api.common.resource.composite.manpag.service.login.ManPagLoginService;
import com.emoldino.framework.util.BeanUtils;

public class EmoldinoLogoutHandler extends SecurityContextLogoutHandler {

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		HttpSession session = request.getSession(false);
		BeanUtils.get(ManPagLoginService.class).logout(ManPagLogoutIn.builder().sessionId(session == null ? null : session.getId()).build());
		super.logout(request, response, authentication);
	}

}
