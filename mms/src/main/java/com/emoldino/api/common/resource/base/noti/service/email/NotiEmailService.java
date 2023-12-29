package com.emoldino.api.common.resource.base.noti.service.email;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.mail.AuthenticationFailedException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.noti.dto.NotiEmailSendIn;
import com.emoldino.api.common.resource.base.noti.repository.noti.Noti;
import com.emoldino.api.common.resource.base.noti.repository.noticontent.NotiContent;
import com.emoldino.framework.repository.cachedata.CacheData;
import com.emoldino.framework.repository.cachedata.CacheDataRepository;
import com.emoldino.framework.repository.cachedata.QCacheData;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

@Service
public class NotiEmailService {

	@Autowired
	private JavaMailSender mailSender;

	public MimeMessage createMimeMessage() {
		return mailSender.createMimeMessage();
	}

	public void post(Noti noti, NotiContent ctt, MultiValueMap<Long, String> emailIdsByCompany) {
		if (!ctt.isEmailEnabled()) {
			return;
		}

		String title = ValueUtils.toString(ctt.getEmailTitle(), ctt.getTitle(), ctt.getContent());
		String content = ValueUtils.toString(ctt.getEmailContent(), ctt.getContent());
		String subtype = ctt.getEmailSubtype();

		List<String> to = new ArrayList<>();

		emailIdsByCompany.forEach((companyId, emails) -> {
			emails.forEach(email -> {
				to.add(email);
				if (emails.size() < 100) {
					return;
				}

				send("eMoldino<noreply@emoldino.com>", to, title, content, subtype);
				to.clear();
			});
		});

		send("eMoldino<noreply@emoldino.com>", to, title, content, subtype);
		to.clear();
	}

	private void send(String from, List<String> to, String title, String content, String subtype) {
		LogicUtils.assertNotEmpty(from, "from");
		if (ObjectUtils.isEmpty(to)) {
			return;
		}
		NotiEmailSendIn input = NotiEmailSendIn.builder()//
				.from(from)//
				.to(to)//
				.title(title)//
				.content(content)//
				.subtype(subtype)//
				.build();
		if (ConfigUtils.isProdMode()) {
			CacheDataUtils.save("emailTask", UUID.randomUUID().toString(), input);
		} else {
			send(input);
		}
	}

	public void sendBatch() {
		QCacheData table = QCacheData.cacheData;
		List<CacheData> emailTasks = new ArrayList<>();
		List<MimeMessage> messages = new ArrayList<>();
		while (TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class).exists(table.cacheName.eq("emailTask").and(table.notIn(emailTasks))))) {
			TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class)//
					.findAll(table.cacheName.eq("emailTask"), PageRequest.of(0, 100, Direction.ASC, "id")))//
					.forEach(emailTask -> {
						try {
							NotiEmailSendIn input = ValueUtils.fromJsonStr(emailTask.getContent(), NotiEmailSendIn.class);
							String from = input.getFrom();
							String title = input.getTitle();
							String content = input.getContent();
							String subtype = input.getSubtype();
							input.getTo().forEach(to -> {
								MimeMessage message = mailSender.createMimeMessage();
								try {
									MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
									helper.setFrom(from);
									helper.setTo(to);
									helper.setSubject(title);
									helper.setText(content, subtype != null && StringUtils.containsIgnoreCase(subtype, "html"));
								} catch (Exception e) {
									LogUtils.saveErrorQuietly(ErrorType.SYS, "EMAIL_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
									return;
								}
								messages.add(message);
							});
						} catch (Exception e) {
							LogUtils.saveErrorQuietly(ErrorType.SYS, "EMAIL_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
							return;
						} finally {
							emailTasks.add(emailTask);
						}

						if (messages.size() >= 100) {
							sendQuietly(messages);
							messages.clear();
							TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class).deleteInBatch(emailTasks));
							emailTasks.clear();
						}
					});
		}
		sendQuietly(messages);
		messages.clear();
		TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class).deleteInBatch(emailTasks));
		emailTasks.clear();
	}

	public void send(NotiEmailSendIn input) {
		if (ObjectUtils.isEmpty(input.getTo())) {
			return;
		}

		String from = input.getFrom();
		String title = input.getTitle();
		String content = input.getContent();
		String subtype = input.getSubtype();

		List<MimeMessage> messages = new ArrayList<>();
		input.getTo().forEach(to -> {
			MimeMessage message = mailSender.createMimeMessage();
			try {
				MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
				helper.setFrom(from);
				helper.setTo(to);
				helper.setSubject(title);
				helper.setText(content, subtype != null && StringUtils.containsIgnoreCase(subtype, "html"));
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "EMAIL_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
				return;
			}
			messages.add(message);
		});

		sendQuietly(messages);
	}

	public void sendQuietly(List<MimeMessage> messages) {
		if (ObjectUtils.isEmpty(messages)) {
			return;
		}
		sendQuietly(messages.toArray(new MimeMessage[messages.size()]));
	}

	public void sendQuietly(MimeMessage... message) {
		try {
			int i = 0;
			do {
				try {
					SyncCtrlUtils.wrap("NotiEmailService.send", () -> mailSender.send(message));
					return;
				} catch (Exception e) {
					if (i++ < 10 && //
							((e instanceof AuthenticationFailedException) || (e.getCause() != null && e.getCause() instanceof AuthenticationFailedException))) {
						LogUtils.saveErrorQuietly(ErrorType.SYS, "EMAIL_TRY_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
						Thread.sleep(2000);
						continue;
					}
					throw e;
				}
			} while (true);
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "EMAIL_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}

}
