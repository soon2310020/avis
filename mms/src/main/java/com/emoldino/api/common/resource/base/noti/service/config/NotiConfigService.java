package com.emoldino.api.common.resource.base.noti.service.config;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.noti.dto.NotiConfig;
import com.emoldino.api.common.resource.base.noti.dto.NotiGetIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiTemplate;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientPriority;
import com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig.NotiRecipientConfig;
import com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig.NotiRecipientConfigRepository;
import com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig.QNotiRecipientConfig;
import com.emoldino.api.common.resource.base.noti.util.NotiUtils;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.common.enumeration.Language;

@Service
public class NotiConfigService {
	private Map<NotiCode, NotiConfig> CONFIGS = new TreeMap<>();

	public ListOut<NotiConfig> getConfigs(NotiGetIn input) {
		init();
		return new ListOut<>(new ArrayList<>(CONFIGS.values()));
	}

	public NotiConfig getConfig(NotiCode notiCode) {
		LogicUtils.assertNotNull(notiCode, "notiCode");
		init();
		return CONFIGS.get(notiCode);
	}

	public ListOut<NotiRecipientConfig> getRecipientConfigs(NotiCode notiCode) {
		LogicUtils.assertNotNull(notiCode, "notiCode");
		return new ListOut<>(BeanUtils.get(NotiRecipientConfigRepository.class).findAll(//
				new BooleanBuilder().and(QNotiRecipientConfig.notiRecipientConfig.notiCode.eq(notiCode)), //
				Sort.by(Direction.ASC, "position")//
		));
	}

	public void postRecipientConfigs(NotiCode notiCode, ListIn<NotiRecipientConfig> input) {
		if (!ObjectUtils.isEmpty(input.getContent())) {
			for (NotiRecipientConfig item : input.getContent()) {
				LogicUtils.assertNotNull(item.getRecipientType(), "recipientType");
				LogicUtils.assertNotNull(item.getRecipientId(), "recipientId");
			}
		}

		BeanUtils.get(NotiRecipientConfigRepository.class).deleteAll(//
				BeanUtils.get(NotiRecipientConfigRepository.class).findAll(//
						new BooleanBuilder().and(QNotiRecipientConfig.notiRecipientConfig.notiCode.eq(notiCode)), //
						Sort.by(Direction.ASC, "position")//
				)//
		);

		if (!ObjectUtils.isEmpty(input.getContent())) {
			int i = 0;
			for (NotiRecipientConfig item : input.getContent()) {
				item.setNotiCode(notiCode);
				item.setPosition(++i);
				if (item.getRecipientPriority() == null) {
					item.setRecipientPriority(NotiRecipientPriority.PRIMARY);
				}
			}
			BeanUtils.get(NotiRecipientConfigRepository.class).saveAll(input.getContent());
		}
	}

//	private static final Map<String, NotiTemplate> TEMPLATES = new LinkedHashMap<>(100, 0.75f, true);

	public NotiTemplate getTemplate(NotiCode notiCode, Language language) {
		LogicUtils.assertNotNull(notiCode, "notiCode");
		LogicUtils.assertNotNull(language, "language");

		// TODO Cache of Noti Template

		String key = "NotiConfigService.template." + notiCode.name() + "." + language.name();
		@SuppressWarnings("unchecked")
		Optional<NotiTemplate> optional = (Optional<NotiTemplate>) ThreadUtils.getProp(key);
		if (optional != null) {
			return ValueUtils.get(optional);
		}

		NotiTemplate template = _getTemplate(notiCode, language);

		ThreadUtils.setProp(key, Optional.ofNullable(template));
		return template;
	}

	private static NotiTemplate _getTemplate(NotiCode notiCode, Language language) {
		String code = notiCode.name();
		String lang = language.name().toLowerCase();

		NotiTemplate template = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/" + lang + "/noti-template.json", NotiTemplate.class);
		if (template == null && !Language.EN.equals(language)) {
			template = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/en/noti-template.json", NotiTemplate.class);
		}
		if (template == null) {
			throw new LogicException("NOTI_TEMPLATE_NOT_FOUND", Property.builder().name("notiCode").value(notiCode).build());
		}

		String content = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/" + lang + "/content.txt", String.class);
		if (!ObjectUtils.isEmpty(content)) {
			template.setContent(content);
		} else if (!Language.EN.equals(language)) {
			content = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/en/content.txt", String.class);
			if (!ObjectUtils.isEmpty(content)) {
				template.setContent(content);
			}
		}
		String emailContent = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/" + lang + "/email-content.html", String.class);
		if (!ObjectUtils.isEmpty(emailContent)) {
			template.setEmailSubtype("text/html");
			template.setEmailContent(emailContent);
		} else if (!Language.EN.equals(language)) {
			emailContent = ResourceUtils.toRequiredType("classpath:noti/" + code + "/template/en/email-content.html", String.class);
			if (!ObjectUtils.isEmpty(emailContent)) {
				template.setEmailSubtype("text/html");
				template.setEmailContent(emailContent);
			}
		} else {
			NotiCategory notiCate = NotiUtils.toCategory(notiCode);
			emailContent = ResourceUtils.toRequiredType("classpath:noti/CATEGORY/" + notiCate.name() + "/template/" + lang + "/email-content.html", String.class);
			if (!ObjectUtils.isEmpty(emailContent)) {
				template.setEmailSubtype("text/html");
				template.setEmailContent(emailContent);
			} else if (!Language.EN.equals(language)) {
				emailContent = ResourceUtils.toRequiredType("classpath:noti/CATEGORY/" + notiCate.name() + "/template/en/email-content.html", String.class);
				if (!ObjectUtils.isEmpty(emailContent)) {
					template.setEmailSubtype("text/html");
					template.setEmailContent(emailContent);
				}
			}
		}
		return template;
	}

	private static boolean initialized = false;

	private void init() {
		if (initialized) {
			return;
		}

		SyncCtrlUtils.wrap("initNoti", () -> {
			if (initialized) {
				return;
			}

			for (NotiCode notiCode : NotiCode.values()) {
				NotiConfig config = ResourceUtils.toRequiredType("classpath:noti/" + notiCode.name() + "/noti-config.json", NotiConfig.class);
				if (config == null) {
					continue;
				}
				CONFIGS.put(notiCode, config);
			}

			initialized = true;
		});
	}
}
