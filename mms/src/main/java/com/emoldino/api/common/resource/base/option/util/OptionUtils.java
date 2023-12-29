package com.emoldino.api.common.resource.base.option.util;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.TabConfig;
import com.emoldino.api.common.resource.base.option.repository.optioncontent.OptionContent;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValue;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import saleson.api.tabTable.TabTableRepository;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.SecurityUtils;

public class OptionUtils {

	public static boolean isEnabled(ConfigCategory configCategory) {
		return isEnabled(configCategory, false);
	}

	/**
	 * Get OptionEnabled of ConfigCategory
	 *
	 * @param configCategory
	 * @param defaultValue
	 * @return
	 */
	public static boolean isEnabled(ConfigCategory configCategory, boolean defaultValue) {
		LogicUtils.assertNotNull(configCategory, "configCategory");
		String str = getFieldValue(configCategory, "enabled", defaultValue + "");
		return ValueUtils.toBoolean(str, defaultValue);
	}

	public static String getFieldValue(ConfigCategory configCategory, String fieldName) {
		return getFieldValue(configCategory, fieldName, null);
	}

	public static <T> T getFieldValues(ConfigCategory configCategory, Class<T> requiredType) {
		return BeanUtils.get(OptionService.class).getFieldValues(configCategory, requiredType);
	}

	/**
	 * Get OptionFieldValue of ConfigCategory
	 *
	 * @param configCategory
	 * @param fieldName
	 * @param defaultValue
	 * @return
	 */
	public static String getFieldValue(ConfigCategory configCategory, String fieldName, String defaultValue) {
		Optional<OptionFieldValue> optional = ThreadUtils.getProp("OptionUtils.value." + configCategory.name() + "." + fieldName + "." + defaultValue, () -> {
			return BeanUtils.get(OptionService.class).getFieldValue(configCategory, fieldName)//
					.filter(data -> !ObjectUtils.isEmpty(data.getValue()));
		});
		return optional.isPresent() ? optional.get().getValue() : defaultValue;
	}

	public static String getUserFieldValue(ConfigCategory configCategory, String fieldName, String defaultValue) {
		Optional<OptionFieldValue> optional = ThreadUtils.getProp("OptionUtils.userValue." + configCategory.name() + "." + fieldName + "." + defaultValue, () -> {
			return BeanUtils.get(OptionService.class).getUserFieldValue(configCategory, fieldName)//
					.filter(data -> !ObjectUtils.isEmpty(data.getValue()));
		});
		return optional.isPresent() ? optional.get().getValue() : defaultValue;
	}

	public static <T> T getContent(String optionName, Class<T> requiredType, T defaultConfig) {
		LogicUtils.assertNotNull(requiredType, "requiredType");
		OptionContent content = BeanUtils.get(OptionService.class).getContent(optionName).orElse(null);
		T option = content == null || ObjectUtils.isEmpty(content.getContent()) ? defaultConfig : ValueUtils.fromJsonStr(content.getContent(), requiredType);
		return option;
	}

	public static <T> T getContent(Enum<?> optionName, Class<T> requiredType, T defaultConfig) {
		LogicUtils.assertNotNull(requiredType, "requiredType");
		OptionContent content = BeanUtils.get(OptionService.class).getContent(optionName.name()).orElse(null);
		T option = content == null || ObjectUtils.isEmpty(content.getContent()) ? defaultConfig : ValueUtils.fromJsonStr(content.getContent(), requiredType);
		return option;
	}

	public static <T> T getContent(String optionName, TypeReference<T> typeRef) {
		return getContent(optionName, typeRef, null);
	}

	public static <T> T getContent(String optionName, TypeReference<T> typeRef, T defaultConfig) {
		LogicUtils.assertNotNull(typeRef, "typeRef");
		OptionContent content = BeanUtils.get(OptionService.class).getContent(optionName).orElse(null);
		T option = content == null || ObjectUtils.isEmpty(content.getContent()) ? defaultConfig : ValueUtils.fromJsonStr(content.getContent(), typeRef);
		option = ValueUtils.toNotNull(option, typeRef);
		return option;
	}

	public static <T> T getUserContent(String optionName, Class<T> requiredType, T defaultConfig) {
		LogicUtils.assertNotNull(requiredType, "requiredType");
		OptionContent content = BeanUtils.get(OptionService.class).getUserContent(optionName).orElse(null);
		T option = content == null || ObjectUtils.isEmpty(content.getContent()) ? defaultConfig : ValueUtils.fromJsonStr(content.getContent(), requiredType);
		return option;
	}

	public static <T> T getUserContent(String optionName, TypeReference<T> typeRef) {
		return getUserContent(optionName, typeRef, null);
	}

	public static <T> T getUserContent(String optionName, TypeReference<T> typeRef, T defaultConfig) {
		LogicUtils.assertNotNull(typeRef, "typeRef");
		OptionContent content = BeanUtils.get(OptionService.class).getUserContent(optionName).orElse(null);
		T option = content == null || ObjectUtils.isEmpty(content.getContent()) ? defaultConfig : ValueUtils.fromJsonStr(content.getContent(), typeRef);
		option = ValueUtils.toNotNull(option, typeRef);
		return option;
	}

	private static final Map<ObjectType, Map<String, TabConfig>> TABs = new ConcurrentHashMap<>();

	public static Map<String, TabConfig> getDefaultTabConfigs(ObjectType objectType) {
		Map<String, TabConfig> configs = SyncCtrlUtils.wrap("ObjectUtils.tabs." + objectType.name(), TABs, objectType, () -> {
			InputStream is = ResourceUtils.getInputStream("classpath:objecttype/" + objectType.name() + "/tabs.json");
			Map<String, TabConfig> map = ValueUtils.fromJsonStream(is, new TypeReference<Map<String, TabConfig>>() {
			});
			return map;
		});
		return new LinkedHashMap<>(configs);
	}

	public static Map<String, TabConfig> getUserTabConfigs(ObjectType objectType) {
		Map<String, TabConfig> configs = getDefaultTabConfigs(objectType);
		BeanUtils.get(TabTableRepository.class)//
				.findAllByUserIdAndDeletedFalseAndObjectType(SecurityUtils.getUserId(), objectType)//
				.stream()//
				.filter(tabTable -> !configs.containsKey(tabTable.getName()) && !ValueUtils.toBoolean(tabTable.getIsDefaultTab(), true))//
				.forEach(tabTable -> configs.put(tabTable.getName(), new TabConfig(false)));
		return configs;
	}

}
