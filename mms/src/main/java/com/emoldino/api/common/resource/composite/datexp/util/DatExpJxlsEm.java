package com.emoldino.api.common.resource.composite.datexp.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.util.ReflectionUtils;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;

public class DatExpJxlsEm {

	/**
	 * Get Custom Field Value
	 * @param item
	 * @param fieldName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String cfv(Object item, String fieldName) {
		LogicUtils.assertNotNull(item, "item");
		LogicUtils.assertNotEmpty(fieldName, "fieldName");

		int id = System.identityHashCode(item);

		Map<String, String> map = ThreadUtils.getProp("em." + id + ".cfv", () -> {
			Method method = ReflectionUtils.getMethod(item.getClass(), "getCustomFields")//
					.orElseThrow(() -> new LogicException("CUSTOM_FIELDS_METHOD_NOT_FOUND"));

			List<FieldValue> fields;
			try {
				fields = (List<FieldValue>) method.invoke(item);
			} catch (Exception e) {
				throw ValueUtils.toAe(e, null);
			}
			return fields == null ? Collections.emptyMap()//
					: fields.stream()//
							.filter(x -> !ObjectUtils.isEmpty(x.getValues()))//
							.collect(Collectors.toMap(FieldValue::getFieldName, FieldValue::getValue));
		});

		return Objects.requireNonNull(map).get(fieldName);
	}

	/**
	 * Get List Item Value
	 * @param list
	 * @param index
	 * @param fieldName
	 * @return
	 */
	public Object liv(List<?> list, int index, String fieldName) {
		if (ObjectUtils.isEmpty(list) || list.size() <= index) {
			return null;
		}
		Object item = list.get(index);
		Object value = ValueUtils.get(item, fieldName);
		return value;
	}

	public String title(SizeUnit unit) {
		if (unit == null) {
			return null;
		}
		return SizeUnit.M.equals(unit) || SizeUnit.CM.equals(unit) || SizeUnit.MM.equals(unit) ? unit.getTitle() + "3" : unit.getTitle();
	}

	public String title(WeightUnit unit) {
		if (unit == null) {
			return null;
		}
		return unit.getTitle();
	}

}
