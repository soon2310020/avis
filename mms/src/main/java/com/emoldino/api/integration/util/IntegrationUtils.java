package com.emoldino.api.integration.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.auth.AuthService;
import saleson.common.config.Const.ERROR_CODE;
import saleson.model.User;

@Slf4j
public class IntegrationUtils {

	public static void checkAuth() {
		HttpServletRequest req = HttpUtils.getRequest();
		if (req == null) {
			throw new BizException(ERROR_CODE.USER_OF_SESSION_NOT_FOUND);
		}
		HttpSession session = req.getSession(false);
		log.info("Integration Session ID: " + (session == null ? "null" : session.getId()));
		User currentUser = BeanUtils.get(AuthService.class).getCurrentUser(req);
		if (currentUser == null) {
			throw new BizException(ERROR_CODE.USER_OF_SESSION_NOT_FOUND);
		}
	}

}
