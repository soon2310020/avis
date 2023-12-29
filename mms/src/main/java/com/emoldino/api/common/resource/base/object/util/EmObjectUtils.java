package com.emoldino.api.common.resource.base.object.util;

import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldValueGetIn;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.api.common.resource.base.object.service.ObjectService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure2ParamsNoReturn;

import saleson.common.enumeration.ObjectType;

public class EmObjectUtils {

	public static void loadCustomFieldValues(ObjectType objectType, List<Long> objectId, Closure2ParamsNoReturn<Long, List<FieldValue>> closure) {
		if (ObjectUtils.isEmpty(objectId)) {
			return;
		}
		getCustomFieldValues(objectType, objectId).forEach(closure::execute);
	}

	private static Map<Long, List<FieldValue>> getCustomFieldValues(ObjectType objectType, List<Long> objectId) {
		return BeanUtils.get(ObjectService.class).getCustomFieldValues(CustomFieldValueGetIn.builder()//
				.objectType(objectType)//
				.objectId(objectId)//
				.build());
	}

}
