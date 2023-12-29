package com.emoldino.api.common.resource.base.noti.service.fcm;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.LoginHistRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.QLoginHist;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.noti.repository.noti.Noti;
import com.emoldino.api.common.resource.base.noti.repository.noticontent.NotiContent;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import saleson.api.user.UserRepository;
import saleson.model.User;

@Service
public class NotiFcmService {
	private FirebaseApp app = null;
	private FirebaseMessaging messaging = null;

	public void post(Noti noti, NotiContent ctt, List<Long> userIds) {
		try {
			List<String> tokens = new ArrayList<>();
			{
				QLoginHist table = QLoginHist.loginHist;
				Instant instant = DateUtils2.getInstant();
				userIds.forEach(userId -> {
					BeanUtils.get(LoginHistRepository.class).findAll(table.userId.eq(userId).and(table.expiredAt.goe(instant)))//
							.forEach(loginHist -> {
								if (ObjectUtils.isEmpty(loginHist.getMessagingToken())) {
									return;
								}
								tokens.add(loginHist.getMessagingToken());
							});
				});
			}
			if (tokens.isEmpty()) {
				return;
			}

			init();

			MulticastMessage multiMsg = MulticastMessage.builder()//
					.addAllTokens(tokens)//
					.setNotification(Notification.builder()//
							.setTitle(ctt.getTitle())//
							.setBody(ctt.getContent())//
							.build())//
					.putData("id", ValueUtils.toString(noti.getId()))//
					.putData("notiCode", noti.getNotiCode() == null ? null : noti.getNotiCode().name())//
					.putData("notiCategory", noti.getNotiCategory() == null ? null : noti.getNotiCategory().name())//
					.putData("notiPriority", noti.getNotiPriority() == null ? null : noti.getNotiPriority().name())//
					.putData("senderId", ValueUtils.toString(noti.getSenderId()))//
					.putData("senderName", getName(noti.getSenderId()))//
					.putData("sentDateTime", DateUtils2.format(noti.getSentAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, HttpUtils.getTimeZone()))//
					.putData("linkType", ctt.getLinkType() == null ? null : ctt.getLinkType().name())//
					.putData("linkTo", ctt.getLinkTo() == null ? null : ctt.getLinkTo())//
					.build();

			messaging.sendMulticast(multiMsg);
		} catch (FirebaseMessagingException e) {
			LogUtils.saveErrorQuietly(e);
		}
	}

	private static String getName(Long userId) {
		if (userId == null) {
			return null;
		}
		return ThreadUtils.getProp("User.name." + userId, () -> {
			User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
			return user.getName();
		});
	}

	private void init() {
		if (messaging == null) {
			SyncCtrlUtils.wrap("NotiFcmService.init", () -> {
				if (app == null) {
					InputStream is = null;
					try {
						is = ResourceUtils.getInputStream("classpath:noti/fcm-key.json");
						GoogleCredentials credentials = GoogleCredentials.fromStream(is);
						FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
						app = FirebaseApp.initializeApp(options);
					} catch (IOException e) {
						AbstractException ae = ValueUtils.toAe(e, null);
						throw ae;
					} finally {
						ValueUtils.closeQuietly(is);
					}
				}

				if (messaging == null) {
					messaging = FirebaseMessaging.getInstance(app);
				}
			});
		}
	}

//	@Value("${notification.fcmKey}")
//	private String fcmKey;
//
//	@Value("${notification.fcmUrl}")
//	private String fcmUrl;
//
//	@Value("${api.expired.time}")
//	private Integer expiredTime;
//
//	private void postLegacy(NotiFcmPostIn input) {
//		HttpHeaders httpHeaders = new HttpHeaders();
//		httpHeaders.set("Authorization", "key=" + fcmKey);
//		httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
//		HttpUtils.call(HttpMethod.POST, fcmUrl, null, null, input, httpHeaders, null, null, 0);
//	}

}
