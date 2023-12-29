package com.emoldino.api.common.resource.base.object.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldValueGetIn;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.api.common.resource.base.object.repository.ObjectRepository;
import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.base.object.repository.customfieldvalue.CustomFieldValueRepository;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.Predicate;

import saleson.common.enumeration.ObjectType;
import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;

@Service
@Transactional
public class ObjectService {

	public Map<Long, List<FieldValue>> getCustomFieldValues(CustomFieldValueGetIn input) {
		return BeanUtils.get(ObjectRepository.class).findCustomFieldValues(input);
	}

	private List<FieldValue> getCustomFieldValue(CustomFieldValueGetIn input) {
		LogicUtils.assertNotNull(input.getObjectType(), "objectType");
		LogicUtils.assertNotEmpty(input.getObjectId(), "objectId");
		if (input.getObjectId().size() > 1) {
			throw new LogicException("ObjectId size should be one");
		}
		return BeanUtils.get(ObjectRepository.class).findCustomFieldValues(input).get(input.getObjectId().get(0));
	}

	public void saveCustomFieldValues(ObjectType objectType, Long objectId, List<FieldValue> values) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotNull(objectId, "objectId");
		Map<String, FieldValue> map = getCustomFieldValue(CustomFieldValueGetIn.builder()//
				.objectType(objectType)//
				.objectId(Arrays.asList(objectId))//
				.build()).stream().collect(Collectors.toMap(item -> item.getFieldName(), item -> item));
		List<FieldValue> changed = new ArrayList<>();
		values.forEach(value -> {
			if (!map.containsKey(value.getFieldName())) {
				return;
			}
			FieldValue oldValue = map.get(value.getFieldName());
			if (ValueUtils.equals(value.getValues(), oldValue.getValues())) {
				return;
			}
			oldValue.setValues(value.getValues());
			changed.add(oldValue);
		});
		changed.forEach(fieldValue -> {
			Predicate predicate = Qs.customFieldValue.customField.id.eq(fieldValue.getFieldId())//
					.and(Qs.customFieldValue.objectId.eq(objectId));
			BeanUtils.get(CustomFieldValueRepository.class).deleteAll(BeanUtils.get(CustomFieldValueRepository.class).findAll(predicate));
			if (ObjectUtils.isEmpty(fieldValue.getValues())) {
				return;
			}
			fieldValue.getValues().forEach(str -> {
				CustomField cf = BeanUtils.get(CustomFieldRepository.class).findById(fieldValue.getFieldId()).orElse(null);
				if (cf == null) {
					return;
				}
				BeanUtils.get(CustomFieldValueRepository.class).save(CustomFieldValue.builder()//
						.objectId(objectId)//
						.customField(cf)//
						.value(str)//
						.build());
			});
		});
	}

}
