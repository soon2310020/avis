package com.emoldino.api.common.resource.base.option.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.option.repository.optioncontent.OptionContent;
import com.emoldino.api.common.resource.base.option.repository.optioncontent.OptionContentRepository;
import com.emoldino.api.common.resource.base.option.repository.optioncontent.QOptionContent;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValue;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValueRepository;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.QOptionFieldValue;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.common.enumeration.ConfigCategory;
import saleson.common.util.SecurityUtils;

@Transactional
@Service
public class OptionService {

	/**
	 * Get an OptionFieldValue by ConfigCategory and FieldName
	 *
	 * @param configCategory
	 * @param fieldName
	 * @return
	 */
	public Optional<OptionFieldValue> getFieldValue(ConfigCategory configCategory, String fieldName) {
		Optional<OptionFieldValue> value = getFieldValue(configCategory, null, fieldName);
		return value;
	}

	public Optional<OptionFieldValue> getUserFieldValue(ConfigCategory configCategory, String fieldName) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return Optional.empty();
		}
		Optional<OptionFieldValue> value = getFieldValue(configCategory, userId, fieldName);
		return value;
	}

	private Optional<OptionFieldValue> getFieldValue(ConfigCategory configCategory, Long userId, String fieldName) {
		QOptionFieldValue table = QOptionFieldValue.optionFieldValue;
		Optional<OptionFieldValue> value = BeanUtils.get(OptionFieldValueRepository.class)//
				.findOne(new BooleanBuilder()//
						.and(table.configCategory.eq(configCategory))//
						.and(userId == null ? table.userId.isNull() : table.userId.eq(userId))//
						.and(table.fieldName.eq(fieldName))//
						.and(table.deleted.eq(false))//
				);
		return value;
	}

	/**
	 * Get All OptionFieldValues by ConfigCategory
	 *
	 * @param configCategory
	 * @return
	 */
	public Map<String, Object> getFieldValues(ConfigCategory configCategory) {
		Map<String, Object> data = getFieldValues(configCategory, (Long) null);
		return data;
	}

	public Map<String, Object> getUserFieldValues(ConfigCategory configCategory) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> data = getFieldValues(configCategory, userId);
		return data;
	}

	private Map<String, Object> getFieldValues(ConfigCategory configCategory, Long userId) {
		LogicUtils.assertNotNull(configCategory, "configCategory");
		Map<String, Object> data = new LinkedHashMap<>();
		QOptionFieldValue table = QOptionFieldValue.optionFieldValue;
		BeanUtils.get(OptionFieldValueRepository.class).findAll(//
				new BooleanBuilder()//
						.and(table.configCategory.eq(configCategory))//
						.and(userId == null ? table.userId.isNull() : table.userId.eq(userId))//
						.and(table.deleted.eq(false)), //
				Sort.by(Direction.ASC, "position")//
		).forEach(item -> {
			Object value = ValueUtils.toObjectByDataTypeStr(item.getValue(), item.getDataType());
			data.put(item.getFieldName(), value);
		});
		return data;
	}

	public <T> T getFieldValues(ConfigCategory configCategory, Class<T> requiredType) {
		T data = getFieldValues(configCategory, null, requiredType);
		return data;
	}

	public <T> T getUserFieldValues(ConfigCategory configCategory, Class<T> requiredType) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return ValueUtils.fromMap(Collections.emptyMap(), requiredType);
		}
		T data = getFieldValues(configCategory, userId, requiredType);
		return data;
	}

	private <T> T getFieldValues(ConfigCategory configCategory, Long userId, Class<T> requiredType) {
		LogicUtils.assertNotNull(configCategory, "configCategory");
		LogicUtils.assertNotNull(requiredType, "requiredType");
		Map<String, Object> map = getFieldValues(configCategory, userId);
		T data = ValueUtils.fromMap(map, requiredType);
		return data;
	}

	public void saveFieldValue(ConfigCategory configCategory, String fieldName, Object value) {
		saveFieldValue(configCategory, fieldName, null, value);
	}

	public void saveUserFieldValue(ConfigCategory configCategory, String fieldName, Object value) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		saveFieldValue(configCategory, fieldName, userId, value);
	}

	private void saveFieldValue(ConfigCategory configCategory, String fieldName, Long userId, Object value) {
		LogicUtils.assertNotNull(configCategory, "configCategory");
		LogicUtils.assertNotNull(fieldName, "fieldName");

		QOptionFieldValue table = QOptionFieldValue.optionFieldValue;
		OptionFieldValue data = BeanUtils.get(OptionFieldValueRepository.class)//
				.findOne(new BooleanBuilder()//
						.and(table.configCategory.eq(configCategory))//
						.and(userId == null ? table.userId.isNull() : table.userId.eq(userId))//
						.and(table.fieldName.eq(fieldName))//
				).orElse(null);

		// Already Existing Data
		if (data != null) {
			setValue(data, value);
			data.setDeleted(false);
			BeanUtils.get(OptionFieldValueRepository.class).save(data);
			return;
		}

		int[] lastPosition = { 0 };
		BeanUtils.get(OptionFieldValueRepository.class)//
				.findAll(//
						new BooleanBuilder()//
								.and(table.configCategory.eq(configCategory))//
								.and(userId == null ? table.userId.isNull() : table.userId.eq(userId)), //
						PageRequest.of(0, 1, Direction.DESC, "position")//
				).forEach(item -> lastPosition[0] = item.getPosition());

		// New Data
		data = new OptionFieldValue();
		data.setConfigCategory(configCategory);
		data.setUserId(userId);
		data.setFieldName(fieldName);
		setValue(data, value);
		data.setPosition(++lastPosition[0]);
		BeanUtils.get(OptionFieldValueRepository.class).save(data);
	}

	/**
	 * Save(Post/Put) All OptionFieldValues by ConfigCategory
	 *
	 * @param configCategory
	 * @param data
	 */
	public void saveFieldValues(ConfigCategory configCategory, Map<String, Object> data) {
		saveFieldValues(configCategory, null, data);
	}

	public void saveUserFieldValues(ConfigCategory configCategory, Map<String, Object> data) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		saveFieldValues(configCategory, userId, data);
	}

	private void saveFieldValues(ConfigCategory configCategory, Long userId, Map<String, Object> data) {
		// 1. Validation
		LogicUtils.assertNotNull(configCategory, "configCategory");
		LogicUtils.assertNotNull(data, "data");

		// 2. Logic

		// 2.1 Get All of OptionFieldValues including Deleted
		Map<String, OptionFieldValue> map = new LinkedHashMap<>();
		QOptionFieldValue table = QOptionFieldValue.optionFieldValue;
		BeanUtils.get(OptionFieldValueRepository.class).findAll(//
				new BooleanBuilder()//
						.and(table.configCategory.eq(configCategory))//
						.and(userId == null ? table.userId.isNull() : table.userId.eq(userId)), //
				Sort.by(Direction.ASC, "position")//
		).forEach(item -> map.put(item.getFieldName(), item));

		// 2.2 Set New Values
		List<OptionFieldValue> list = new ArrayList<>();
		int[] i = { 0 };
		data.forEach((fieldName, value) -> {
			if (value != null && value instanceof Collection) {
				return;
			}

			OptionFieldValue item;
			if (map.containsKey(fieldName)) {
				item = map.remove(fieldName);
				item.setDeleted(false);
			} else {
				item = new OptionFieldValue();
				item.setConfigCategory(configCategory);
				item.setUserId(userId);
				item.setFieldName(fieldName);
			}
			setValue(item, value);
			item.setPosition(++i[0]);

			list.add(item);
		});

		// 2.3 Set Deleted Values
		map.values().forEach(item -> {
			item.setPosition(++i[0]);
			item.setDeleted(true);
			list.add(item);
		});

		// 3. Finalization
		if (list.isEmpty()) {
			return;
		}
		BeanUtils.get(OptionFieldValueRepository.class).saveAll(list);
	}

	private static void setValue(OptionFieldValue item, Object value) {
		if (value == null) {
			item.setDataType(null);
			item.setValue(null);
			return;
		}
		String dataType = ValueUtils.getDataTypeStr(value);
		item.setDataType(dataType);
		if (value instanceof Boolean) {
			item.setValue((Boolean) value ? "Y" : "N");
		} else {
			item.setValue(ValueUtils.toString(value));
		}
	}

	public Optional<OptionContent> getContent(String optionName) {
		Optional<OptionContent> optional = getContent(optionName, null);
		return optional;
	}

	public void saveContent(Enum<?> optionName, Object content) {
		saveContent(optionName.name(), null, content);
	}

	public void saveContent(String optionName, Object content) {
		saveContent(optionName, null, content);
	}

	public Optional<OptionContent> getUserContent(String optionName) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return Optional.empty();
		}
		Optional<OptionContent> optional = getContent(optionName, userId);
		return optional;
	}

	public void saveUserContent(String optionName, Object content) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		saveContent(optionName, userId, content);
	}

	private Optional<OptionContent> getContent(String optionName, Long userId) {
		Optional<OptionContent> optional = BeanUtils.get(OptionContentRepository.class).findByOptionNameAndUserId(optionName, userId);
		return optional;
	}

	private void saveContent(String optionName, Long userId, Object content) {
		LogicUtils.assertNotNull(optionName, "optionName");

		QOptionContent table = QOptionContent.optionContent;
		OptionContent data = BeanUtils.get(OptionContentRepository.class)//
				.findOne(new BooleanBuilder()//
						.and(table.optionName.eq(optionName))//
						.and(userId == null ? table.userId.isNull() : table.userId.eq(userId))//
				).orElse(null);
		if (data == null) {
			data = new OptionContent();
			data.setUserId(userId);
			data.setOptionName(optionName);
		}
		if (ValueUtils.isPrimitiveType(content)) {
			data.setContent(ValueUtils.toString(content));
		} else {
			String str = ValueUtils.toJsonStr(content);
			data.setContent(str);
		}
		BeanUtils.get(OptionContentRepository.class).save(data);
	}

}
