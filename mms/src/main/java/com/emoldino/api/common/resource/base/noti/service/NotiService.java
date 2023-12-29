package com.emoldino.api.common.resource.base.noti.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.QRoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUserRepository;
import com.emoldino.api.common.resource.base.noti.dto.NotiConfig;
import com.emoldino.api.common.resource.base.noti.dto.NotiGetIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn.NotiPostRecipient;
import com.emoldino.api.common.resource.base.noti.dto.NotiTemplate;
import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientPriority;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientType;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;
import com.emoldino.api.common.resource.base.noti.repository.noti.Noti;
import com.emoldino.api.common.resource.base.noti.repository.noti.NotiRepository;
import com.emoldino.api.common.resource.base.noti.repository.noti.QNoti;
import com.emoldino.api.common.resource.base.noti.repository.noticontent.NotiContent;
import com.emoldino.api.common.resource.base.noti.repository.noticontent.NotiContentRepository;
import com.emoldino.api.common.resource.base.noti.repository.noticontent.QNotiContent;
import com.emoldino.api.common.resource.base.noti.repository.notirecipient.NotiRecipient;
import com.emoldino.api.common.resource.base.noti.repository.notirecipient.NotiRecipientRepository;
import com.emoldino.api.common.resource.base.noti.repository.notirecipient.QNotiRecipient;
import com.emoldino.api.common.resource.base.noti.service.config.NotiConfigService;
import com.emoldino.api.common.resource.base.noti.service.email.NotiEmailService;
import com.emoldino.api.common.resource.base.noti.service.fcm.NotiFcmService;
import com.emoldino.api.common.resource.base.noti.util.NotiUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobCall;
import com.emoldino.framework.util.JobUtils.JobCallParam;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.user.UserRepository;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.Language;
import saleson.common.enumeration.PriorityType;
import saleson.common.util.SecurityUtils;
import saleson.model.QUser;
import saleson.model.User;

@Service
@Transactional
public class NotiService {

	public void enqueue(NotiCode notiCode, NotiPostIn input) {
		// 1. Get NotiConfig
		NotiConfig config = getConfig(notiCode);
		if (!config.isWebEnabled() && !config.isMobileEnabled() && !config.isEmailEnabled()) {
			return;
		}

		// 2. Populate Noti
		prepopulate(notiCode, config, input);

		// 3. Reserve Noti
		JobCall call = new JobCall();
		call.setLogicName(ReflectionUtils.toName(NotiService.class, "post"));
		call.getParams().add(new JobCallParam("notiCode", NotiCode.class, notiCode));
		call.getParams().add(new JobCallParam("input", NotiPostIn.class, input));
		JobUtils.enqueue("notiTaskExecutor", UUID.randomUUID().toString(), false, call, null, null, null);
	}

	public void post(NotiCode notiCode, NotiPostIn input) {
		// 1. Get NotiConfig
		NotiConfig config = getConfig(notiCode);
		if (!config.isWebEnabled() && !config.isMobileEnabled() && !config.isEmailEnabled()) {
			return;
		}

		// 2. Populate Noti
		prepopulate(notiCode, config, input);
		populate(notiCode, config, input);
		NotiCategory notiCate = NotiUtils.toCategory(notiCode);

		// 3. Get Recipients User IDs
		Map<Long, NotiPostRecipient> recpMap = new LinkedHashMap<>();
		MultiValueMap<Language, Long> userIdsByLang = new LinkedMultiValueMap<>();
		putUserRecps(input.getRecipients(), recpMap, userIdsByLang);

		// 4. Post Noti
		Noti noti = new Noti();
		noti.setNotiCode(notiCode);
		noti.setNotiCategory(notiCate);
		noti.setNotiPriority(input.getNotiPriority());
		noti.setDataId(input.getDataId());
		noti.setSenderId(input.getSenderId());
		noti.setSentAt(DateUtils2.getInstant());
		BeanUtils.get(NotiRepository.class).save(noti);

		// 5. Post Noti Recipients
		postUsers(notiCode, noti.getId(), input, userIdsByLang);
	}

	private static void prepopulate(NotiCode notiCode, NotiConfig config, NotiPostIn input) {
		if (input.getSenderId() == null) {
			input.setSenderByEmail("support@emoldino.com");
		}
		if (input.getSenderId() == null) {
			input.setSenderId(SecurityUtils.getUserId());
		}
	}

	private static Map<String, Object> populate(NotiCode notiCode, NotiConfig config, NotiPostIn input) {
		if (ObjectUtils.isEmpty(input.getRecipients())) {
			BeanUtils.get(NotiConfigService.class).getRecipientConfigs(notiCode).getContent().forEach(recipient -> {
				input.addRecipient(NotiPostRecipient.builder()//
						.recipientType(recipient.getRecipientType())//
						.recipientId(recipient.getRecipientId())//
						.recipientPriority(recipient.getRecipientPriority())//
						.build()//
				);
			});
		}
		if (ObjectUtils.isEmpty(input.getRecipients()) && config != null && !ObjectUtils.isEmpty(config.getRecipients())) {
			input.setRecipients(config.getRecipients());
		}

		if (input.getNotiPriority() == null) {
			input.setNotiPriority(config.getNotiPriority() == null ? PriorityType.LOW : config.getNotiPriority());
		}

		if (input.getWebEnabled() == null) {
			input.setWebEnabled(config.isWebEnabled());
		}
		if (input.getMobileEnabled() == null) {
			input.setMobileEnabled(config.isMobileEnabled());
		}
		if (input.getEmailEnabled() == null) {
			input.setEmailEnabled(config.isEmailEnabled());
		}

		if (ObjectUtils.isEmpty(input.getLinkTo())) {
			input.setLinkType(config.getLinkType());
			input.setLinkTo(config.getLinkTo());
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> params = ValueUtils.toRequiredType(input, Map.class);
		params.putAll(input.getParams() == null ? Collections.emptyMap() : input.getParams());

		if (!ObjectUtils.isEmpty(input.getLinkTo())) {
			String linkTo = input.getLinkTo();
			StringWriter writer = new StringWriter();
			VelocityContext context = new VelocityContext(params);
			context.put(linkTo, input);
			VE.evaluate(context, writer, "noti-template.linkTo", linkTo);
			input.setLinkTo(writer.toString());
		}

		return params;
	}

	private static void putUserRecps(List<NotiPostRecipient> recipients, Map<Long, NotiPostRecipient> recpMap, MultiValueMap<Language, Long> userIdsByLangMap) {
		if (ObjectUtils.isEmpty(recipients)) {
			return;
		}
		for (NotiPostRecipient recp : recipients) {
			if (NotiRecipientType.USER.equals(recp.getRecipientType())) {
				putUserRecp(recp, recpMap, userIdsByLangMap);
			} else if (NotiRecipientType.ROLE.equals(recp.getRecipientType())) {
				DataUtils.runEach(RoleUserRepository.class, //
						QRoleUser.roleUser.roleId.eq(recp.getRecipientId()), //
						Sort.by("id"), 100, true, roleUser -> {
							Long userId = roleUser.getUserId();
							putUserRecp(//
									NotiPostRecipient.builder()//
											.recipientType(NotiRecipientType.USER)//
											.recipientId(userId)//
											.recipientPriority(recp.getRecipientPriority())//
											.build(), //
									recpMap, userIdsByLangMap//
							);
						});
			} else if (NotiRecipientType.EMOLDINO_ADMIN.equals(recp.getRecipientType()) || NotiRecipientType.OEM_ADMIN.equals(recp.getRecipientType())
					|| NotiRecipientType.SUPPLIER_ADMIN.equals(recp.getRecipientType())) {
				boolean emoldino = false;
				CompanyType companyType = null;
				if (NotiRecipientType.EMOLDINO_ADMIN.equals(recp.getRecipientType())) {
					emoldino = true;
					companyType = CompanyType.IN_HOUSE;
				} else if (NotiRecipientType.OEM_ADMIN.equals(recp.getRecipientType())) {
					emoldino = false;
					companyType = CompanyType.IN_HOUSE;
				} else if (NotiRecipientType.SUPPLIER_ADMIN.equals(recp.getRecipientType())) {
					emoldino = false;
					companyType = CompanyType.SUPPLIER;
				}

				QUser qUser = QUser.user;
				DataUtils.runBatch(BeanUtils.get(JPAQueryFactory.class)//
						.select(qUser.id)//
						.from(qUser)//
						.innerJoin(Q.company).on(//
								Q.company.id.eq(qUser.companyId)//
										.and(Q.company.isEmoldino.eq(emoldino))//
										.and(Q.company.companyType.eq(companyType))//
										.and(QueryUtils.isCompany()))//
						.where(//
								qUser.admin.eq(true)//
										.and(qUser.deleted.isNull().or(qUser.deleted.isFalse()))//
										.and(qUser.enabled.isNull().or(qUser.enabled.isTrue()))//
						), //
						100, true, userId -> {
							putUserRecp(//
									NotiPostRecipient.builder()//
											.recipientType(NotiRecipientType.USER)//
											.recipientId(userId)//
											.recipientPriority(recp.getRecipientPriority())//
											.build(), //
									recpMap, userIdsByLangMap//
							);
						});
			}
		}
	}

	private static void putUserRecp(NotiPostRecipient recp, Map<Long, NotiPostRecipient> recpMap, MultiValueMap<Language, Long> userIdsByLangMap) {
		Long userId = recp.getRecipientId();
		if (userId == null) {
			return;
		} else if (recpMap.containsKey(userId)) {
			if (NotiRecipientPriority.ADDITIONAL.equals(recp.getRecipientPriority())) {
				return;
			}
			NotiPostRecipient recpOld = recpMap.get(userId);
			if (!NotiRecipientPriority.ADDITIONAL.equals(recpOld.getRecipientPriority())) {
				return;
			}
			recpMap.put(userId, recp);
			return;
		}
		User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
		if (isUserAvailable(user)) {
			recpMap.put(userId, recp);
			Language lang = getLanguage(user);
			userIdsByLangMap.add(lang, userId);
		}
	}

	private void postUsers(NotiCode notiCode, Long notiId, NotiPostIn input, MultiValueMap<Language, Long> userIdsByLangMap) {
		@SuppressWarnings("unchecked")
		Map<String, Object> params = ValueUtils.toRequiredType(input, Map.class);
		params.putAll(input.getParams() == null ? Collections.emptyMap() : input.getParams());
		String linkUrl = ConfigUtils.getHostUrl() + ValueUtils.toString(input.getLinkTo(), "");
		params.put("linkUrl", linkUrl);
		params.put("notiCode", notiCode);
		params.put("hostUrl", ConfigUtils.getHostUrl());

		int position = 0;
		for (Language lang : userIdsByLangMap.keySet()) {
			NotiTemplate template = getTemplate(notiCode, lang, input);
			List<Long> userIds = userIdsByLangMap.get(lang);

			for (Long userId : userIds) {
				NotiRecipient recp = new NotiRecipient();
				recp.setNotiId(notiId);
				recp.setUserId(userId);
				recp.setLanguage(lang);
				recp.setContentByUser(template.isContentByUser());
				recp.setPosition(position);
				recp.setNotiStatus(NotiStatus.UNREAD);
				BeanUtils.get(NotiRecipientRepository.class).save(recp);

				if (template.isContentByUser()) {
					populateContent(input, template, params);

					NotiContent content = ValueUtils.map(input, NotiContent.class);
					ValueUtils.map(template, content);
					content.setNotiId(notiId);
					content.setLanguage(lang);
					content.setUserId(userId);
					BeanUtils.get(NotiContentRepository.class).save(content);

					if (ConfigUtils.isProdMode()) {
						JobCall call = new JobCall();
						call.setLogicName(ReflectionUtils.toName(NotiService.class, "notifyByUser"));
						call.getParams().add(new JobCallParam("notiId", Long.class, notiId));
						call.getParams().add(new JobCallParam("userId", Long.class, userId));
						JobUtils.enqueue("notiTaskExecutor", UUID.randomUUID().toString(), false, call, null, null, null);
					} else {
						notifyByUser(notiId, userId);
					}
				}
			}

			if (!template.isContentByUser()) {
				populateContent(input, template, params);

				NotiContent content = ValueUtils.map(input, NotiContent.class);
				ValueUtils.map(template, content);
				content.setNotiId(notiId);
				content.setLanguage(lang);
				BeanUtils.get(NotiContentRepository.class).save(content);

				if (ConfigUtils.isProdMode()) {
					JobCall call = new JobCall();
					call.setLogicName(ReflectionUtils.toName(NotiService.class, "notifyByLang"));
					call.getParams().add(new JobCallParam("notiId", Long.class, notiId));
					call.getParams().add(new JobCallParam("lang", Language.class, lang));
					JobUtils.enqueue("notiTaskExecutor", UUID.randomUUID().toString(), false, call, null, null, null);
				} else {
					notifyByLang(notiId, lang);
				}
			}
		}
	}

	public static void notifyByUser(Long notiId, Long userId) {
		Noti noti = BeanUtils.get(NotiRepository.class).findById(notiId).orElse(null);
		if (noti == null) {
			return;
		}
		QNotiRecipient qRecp = QNotiRecipient.notiRecipient;
		NotiRecipient recp = BeanUtils.get(NotiRecipientRepository.class).findOne(qRecp.notiId.eq(notiId).and(qRecp.userId.eq(userId))).orElse(null);
		if (recp == null) {
			return;
		}
		QNotiContent qCtt = QNotiContent.notiContent;
		NotiContent ctt;
		if (recp.isContentByUser()) {
			ctt = BeanUtils.get(NotiContentRepository.class).findOne(qCtt.notiId.eq(notiId).and(qCtt.userId.eq(userId))).orElse(null);
		} else {
			ctt = BeanUtils.get(NotiContentRepository.class).findOne(qCtt.notiId.eq(notiId).and(qCtt.userId.isNull()).and(qCtt.language.eq(recp.getLanguage()))).orElse(null);
		}
		if (ctt == null) {
			return;
		}
		List<Long> userIds = new ArrayList<>();
		MultiValueMap<Long, String> emailsByCompanyId = new LinkedMultiValueMap<>();
		populate(userId, userIds, emailsByCompanyId);
		notify(noti, ctt, userIds, emailsByCompanyId);
	}

	public static void notifyByLang(Long notiId, Language lang) {
		Noti noti = BeanUtils.get(NotiRepository.class).findById(notiId).orElse(null);
		if (noti == null) {
			return;
		}
		QNotiContent qCtt = QNotiContent.notiContent;
		NotiContent ctt = BeanUtils.get(NotiContentRepository.class).findOne(qCtt.notiId.eq(notiId).and(qCtt.userId.isNull()).and(qCtt.language.eq(lang))).orElse(null);
		if (ctt == null) {
			return;
		}
		QNotiRecipient qRecp = QNotiRecipient.notiRecipient;
		List<Long> userIds = new ArrayList<>();
		MultiValueMap<Long, String> emailsByCompanyId = new LinkedMultiValueMap<>();
		BeanUtils.get(NotiRecipientRepository.class)//
				.findAll(qRecp.notiId.eq(notiId).and(qRecp.language.eq(lang)), Sort.by("position"))//
				.forEach(recp -> populate(recp.getUserId(), userIds, emailsByCompanyId));
		notify(noti, ctt, userIds, emailsByCompanyId);
	}

	private static void populate(Long userId, List<Long> userIds, MultiValueMap<Long, String> emailsByCompanyId) {
		User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
		if (!isUserAvailable(user)) {
			return;
		}

		// Put User ID
		userIds.add(userId);

		// Put Email
		if (user.getCompanyId() == null) {
			return;
		}
		String email = user.getEmail();
		if (ObjectUtils.isEmpty(email) || !email.contains("@")) {
			return;
		}
		if (!ObjectUtils.isEmpty(user.getName())) {
			email = user.getName() + "<" + email + ">";
		}
		emailsByCompanyId.add(user.getCompanyId(), email);
	}

	private static boolean isUserAvailable(User user) {
		return user != null && !user.isDeleted() && user.isEnabled();
	}

	private static void notify(Noti noti, NotiContent ctt, List<Long> userIds, MultiValueMap<Long, String> emailsByCompanyId) {
		BeanUtils.get(NotiFcmService.class).post(noti, ctt, userIds);
		BeanUtils.get(NotiEmailService.class).post(noti, ctt, emailsByCompanyId);
	}

	private static final VelocityEngine VE;
	static {
		VE = new VelocityEngine();
		VE.init();
	}

	private static NotiTemplate getTemplate(NotiCode notiCode, Language lang, NotiPostIn input) {
		NotiTemplate template = BeanUtils.get(NotiConfigService.class).getTemplate(notiCode, lang);
		if (template == null) {
			template = new NotiTemplate();
		} else {
			template = ValueUtils.map(template, NotiTemplate.class);
		}
		if (!ObjectUtils.isEmpty(input.getTitle())) {
			template.setTitle(input.getTitle());
		}
		if (!ObjectUtils.isEmpty(input.getContent())) {
			template.setContent(input.getContent());
		}
		if (!ObjectUtils.isEmpty(input.getEmailTitle())) {
			template.setEmailTitle(input.getEmailTitle());
		}
		if (!ObjectUtils.isEmpty(input.getEmailSubtype())) {
			template.setEmailSubtype(input.getEmailSubtype());
		}
		if (!ObjectUtils.isEmpty(input.getEmailContent())) {
			template.setEmailContent(input.getEmailContent());
		}
		return template;
	}

	private static void populateContent(NotiPostIn input, NotiTemplate template, Map<String, Object> params) {
		if (ValueUtils.toBoolean(input.getWebEnabled(), false) || ValueUtils.toBoolean(input.getMobileEnabled(), false)) {
			if (!ObjectUtils.isEmpty(template.getTitle())) {
				StringWriter writer = new StringWriter();
				VelocityContext context = new VelocityContext(params);
				VE.evaluate(context, writer, "noti-template.title", template.getTitle());
				template.setTitle(writer.toString());
				params.put("title", template.getTitle());
			}
			StringWriter writer = new StringWriter();
			VelocityContext context = new VelocityContext(params);
			VE.evaluate(context, writer, "noti-template.content", template.getContent());
			template.setContent(writer.toString());
			params.put("content", template.getContent());
		}
		if (ValueUtils.toBoolean(input.getEmailEnabled(), false)) {
			if (ObjectUtils.isEmpty(template.getEmailTitle())) {
				template.setEmailTitle(template.getContent());
			} else {
				StringWriter writer = new StringWriter();
				VelocityContext context = new VelocityContext(params);
				VE.evaluate(context, writer, "noti-template.email-title", template.getEmailTitle());
				template.setEmailTitle(writer.toString());
			}
			if (ObjectUtils.isEmpty(template.getEmailContent())) {
				template.setEmailContent(template.getEmailTitle());
			} else {
				StringWriter writer = new StringWriter();
				VelocityContext context = new VelocityContext(params);
				VE.evaluate(context, writer, "noti-template.email-content", template.getEmailContent());
				template.setEmailContent(writer.toString());
			}
			if (!ObjectUtils.isEmpty(template.getEmailTitle())) {
				params.put("emailTitle", template.getEmailTitle());
			}
			if (!ObjectUtils.isEmpty(template.getEmailContent())) {
				params.put("emailContent", template.getEmailContent());
			}
		}
	}

	private static NotiConfig getConfig(NotiCode notiCode) {
		ValueUtils.assertNotEmpty(notiCode, "noti_code");

		NotiConfig notiConfig = BeanUtils.get(NotiConfigService.class).getConfig(notiCode);
		if (notiConfig == null) {
			throw new BizException("UNKNOWN_NOTI_CODE", //
					"Cannot find the Noti Config!!: " + notiCode, //
					Property.builder().name("notiCode").value(notiCode).build()//
			);
		}

		return notiConfig;
	}

	public QueryResults<NotiUserItem> getBySession(NotiGetIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return QueryResults.emptyResults();
		}

		JPAQuery<NotiUserItem> query = toUserQuery(input, pageable, userId);
		QueryResults<NotiUserItem> results = query.fetchResults();
		for (NotiUserItem item : results.getResults()) {
			if (!ObjectUtils.isEmpty(item.getContent())) {
				continue;
			}
			NotiContent content = getUserContent(item.getId(), userId);
			if (content != null) {
				item.setContent(content.getContent());
				item.setLinkType(content.getLinkType());
				item.setLinkTo(content.getLinkTo());
			}
		}

		return results;
	}

	private static NotiContent getUserContent(Long id, Long userId) {
		if (id == null || userId == null) {
			return null;
		}
		QNotiContent table = QNotiContent.notiContent;
		NotiContent content = BeanUtils.get(NotiContentRepository.class)//
				.findOne(new BooleanBuilder().and(table.notiId.eq(id).and(table.userId.eq(userId))))//
				.orElse(BeanUtils.get(NotiContentRepository.class)//
						.findOne(new BooleanBuilder().and(table.notiId.eq(id).and(table.userId.isNull()).and(table.language.eq(getLanguage(userId)))))//
						.orElse(BeanUtils.get(NotiContentRepository.class)//
								.findOne(new BooleanBuilder().and(table.notiId.eq(id).and(table.userId.isNull()).and(table.language.eq(Language.EN))))//
								.orElse(//
										null//
								)//
						)//
				);
		return content;
	}

	private static Language getLanguage(Long userId) {
		if (userId == null) {
			return Language.EN;
		}
		return ThreadUtils.getProp("User.lang." + userId, () -> {
			User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
			Language lang = getLanguage(user);
			return lang;
		});
	}

	private static Language getLanguage(User user) {
		Language lang = user == null || ObjectUtils.isEmpty(user.getLanguage()) ? Language.EN : Language.valueOf(user.getLanguage());
		return lang;
	}

	private JPAQuery<NotiUserItem> toUserQuery(NotiGetIn input, Pageable pageable, Long userId) {
		LogicUtils.assertNotNull(userId, "userId");

		Language lang = getLanguage(userId);

		QNoti qNoti = QNoti.noti;
		QNotiRecipient qNotiRecv = QNotiRecipient.notiRecipient;
		QUser qUser = QUser.user;
		QNotiContent qNotiContent = QNotiContent.notiContent;
		QNotiContent qNotiContent2 = new QNotiContent("notiContent2");

		JPAQuery<NotiUserItem> query;
		if (Language.EN.equals(lang)) {
			query = BeanUtils.get(JPAQueryFactory.class)//
					.select(Projections.constructor(NotiUserItem.class, //
							qNoti.id, //
							qNoti.notiCode, //
							qNoti.notiCategory, //
							qNoti.notiPriority, //
							qNoti.senderId, //
							qUser.name, //
							qNoti.sentAt, //
							qNotiRecv.id, //
							qNotiRecv.notiStatus, //
							qNotiRecv.readAt, //
							qNotiContent.content, //
							qNotiContent.linkType, //
							qNotiContent.linkTo//
					))//
					.from(qNoti);
		} else {
			query = BeanUtils.get(JPAQueryFactory.class)//
					.select(Projections.constructor(NotiUserItem.class, //
							qNoti.id, //
							qNoti.notiCode, //
							qNoti.notiCategory, //
							qNoti.notiPriority, //
							qNoti.senderId, //
							qUser.name, //
							qNoti.sentAt, //
							qNotiRecv.id, //
							qNotiRecv.notiStatus, //
							qNotiRecv.readAt, //
							qNotiContent.content, //
							qNotiContent.linkType, //
							qNotiContent.linkTo, //
							qNotiContent2.content, //
							qNotiContent2.linkType, //
							qNotiContent2.linkTo //
					))//
					.from(qNoti)//
					.leftJoin(qNotiContent2).on(qNotiContent2.notiId.eq(qNoti.id)//
							.and(qNotiContent2.userId.isNull())//
							.and(qNotiContent2.language.eq(Language.EN))//
					);//
		}
		query//
				.leftJoin(qNotiContent).on(qNotiContent.notiId.eq(qNoti.id)//
						.and(qNotiContent.userId.isNull())//
						.and(qNotiContent.language.eq(lang))//
				)//
				.innerJoin(qNotiRecv).on(qNotiRecv.notiId.eq(qNoti.id)//
						.and(qNotiRecv.userId.eq(userId))//
						.and(qNotiRecv.deleted.isNull().or(qNotiRecv.deleted.isFalse()))//
				)//				
				.leftJoin(qUser).on(qUser.id.eq(qNoti.senderId))//
				.orderBy(qNoti.sentAt.desc());

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, qNoti.id, input.getId());
		QueryUtils.in(filter, qNoti.notiCategory, input.getNotiCategory());
		QueryUtils.in(filter, qNotiRecv.notiStatus, input.getNotiStatus());
		query.where(filter);
		QueryUtils.applyPagination(query, pageable);

		return query;
	}

	public void readBySession(Long id) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}

		NotiRecipient data = BeanUtils.get(NotiRecipientRepository.class).findById(id).orElse(null);
		if (data == null) {
			throw DataUtils.newDataNotFoundException(NotiRecipient.class, "id", id);
		} else if (!userId.equals(data.getUserId())) {
			throw new BizException("UNPERMITTED_READ");
		}

		data.setNotiStatus(NotiStatus.READ);
		data.setReadAt(DateUtils2.getInstant());
		BeanUtils.get(NotiRecipientRepository.class).save(data);
	}

}
