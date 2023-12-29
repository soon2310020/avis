package com.emoldino.api.common.resource.composite.manpag.service.login;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.LoginHist;
import com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.LoginHistRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.QLoginHist;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLogoutIn;
import com.emoldino.framework.enumeration.ClientType;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.user.UserRepository;
import saleson.common.security.UserPrincipal;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

@Service
@Transactional
public class ManPagLoginService {
	@Value("${api.expired.time}")
	private Long expiredTime;

	public ManPagLoginOut login(ManPagLoginIn input) {
		ManPagLoginOut output = TranUtils.doNewTranQuietly(() -> {
			Long userId = null;
			{
				UserPrincipal userDetail = SecurityUtils.getUserPrincipal();
				if (userDetail != null) {
					userId = userDetail.getId();
					input.setUsername(userDetail.getUsername());
				}
				if (userId == null) {
					if (ObjectUtils.isEmpty(input.getUsername())) {
						return new ManPagLoginOut();
					}

					User user = BeanUtils.get(UserRepository.class).findByLoginIdAndDeletedIsFalse(input.getUsername()).orElse(null);
					if (user == null) {
						return new ManPagLoginOut();
					}
					userId = user.getId();
				}
			}

			LoginHist data = null;
			String sessionId = null;
			Instant expiredAt = null;
			HttpServletRequest req = HttpUtils.getRequest();
			if (req != null) {
				HttpSession session = req.getSession(false);
				if (session != null) {
					sessionId = session.getId();
					expiredAt = DateUtils2.getInstant().plusSeconds(session.getMaxInactiveInterval() + (11 * 60));
				}
			}
			if (sessionId == null) {
				sessionId = input.getSessionId();
			}
			if (sessionId == null) {
				sessionId = UUID.randomUUID().toString();
			} else {
				data = BeanUtils.get(LoginHistRepository.class).findById(sessionId).orElse(null);
			}
			input.setSessionId(sessionId);

			if (expiredAt == null && expiredTime != null) {
				expiredAt = DateUtils2.getInstant().plus(Duration.ofMinutes(expiredTime));
			}

			if (data != null) {
				if (ObjectUtils.isEmpty(input.getClientId())) {
					input.setClientId(data.getClientId());
				}
				ValueUtils.map(input, data);
			} else {
				ClientType clientType;
				if (HttpUtils.isAndroid()) {
					clientType = ClientType.ANDROID;
				} else if (HttpUtils.isiOS()) {
					clientType = ClientType.IOS;
				} else {
					clientType = ClientType.WEB;
				}

				data = ValueUtils.map(input, LoginHist.class);
				data.setUserId(userId);
				data.setClientType(clientType);
				data.setLoginAt(DateUtils2.getInstant());
			}

			expireOldLogins(data);
			data.setExpiredAt(expiredAt);
			BeanUtils.get(LoginHistRepository.class).save(data);

			return new ManPagLoginOut(sessionId);
		});

		return output != null ? output : new ManPagLoginOut();
	}

	public void logout(ManPagLogoutIn input) {
		TranUtils.doNewTranQuietly(() -> {
			QLoginHist table = QLoginHist.loginHist;

			String sessionId = null;
			HttpServletRequest req = HttpUtils.getRequest();
			if (req != null) {
				HttpSession session = req.getSession(false);
				if (session != null) {
					sessionId = session.getId();
				}
			}
			if (sessionId == null) {
				sessionId = input.getSessionId();
			}

			LoginHist data = null;
			if (!ObjectUtils.isEmpty(sessionId)) {
				data = BeanUtils.get(LoginHistRepository.class).findById(sessionId).orElse(null);
			}
			if (data == null && !ObjectUtils.isEmpty(input.getMessagingToken())) {
				Page<LoginHist> page = BeanUtils.get(LoginHistRepository.class).findAll(//
						table.messagingToken.eq(input.getMessagingToken()), //
						PageRequest.of(0, 1, Direction.DESC, "loginAt")//
				);
				data = page.isEmpty() ? null : page.getContent().get(0);
			}
			if (data == null && !ObjectUtils.isEmpty(input.getClientId())) {
				Page<LoginHist> page = BeanUtils.get(LoginHistRepository.class).findAll(//
						table.clientId.eq(input.getClientId()), //
						PageRequest.of(0, 1, Direction.DESC, "loginAt")//
				);
				data = page.isEmpty() ? null : page.getContent().get(0);
			}

			if (data == null || data.getLogoutAt() != null) {
				return;
			}

			data.setLogoutAt(DateUtils2.getInstant());
			if (data.getExpiredAt() == null || data.getExpiredAt().compareTo(data.getLogoutAt()) > 0) {
				data.setExpiredAt(data.getLogoutAt());
			}

			BeanUtils.get(LoginHistRepository.class).save(data);
			expireOldLogins(data);
		});
	}

	private static void expireOldLogins(LoginHist data) {
		if (ObjectUtils.isEmpty(data.getUserId()) || ObjectUtils.isEmpty(data.getMessagingToken())) {
			return;
		}
		Instant instant = DateUtils2.getInstant();
		QLoginHist table = QLoginHist.loginHist;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(table.userId.eq(data.getUserId()))//
				.and(table.messagingToken.eq(data.getMessagingToken()))//
				.and(table.expiredAt.gt(instant));
		if (!ObjectUtils.isEmpty(data.getSessionId())) {
			filter.and(table.sessionId.ne(data.getSessionId()));
		}
		BeanUtils.get(LoginHistRepository.class).findAll(filter, PageRequest.of(0, 50, Direction.DESC, "loginAt"))//
				.forEach(oldData -> {
					oldData.setExpiredAt(instant);
					BeanUtils.get(LoginHistRepository.class).save(oldData);
				});
	}

}
