package com.emoldino.api.common.resource.base.object.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldValueGetIn;
import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class ObjectRepository {

	public Map<Long, List<FieldValue>> findCustomFieldValues(CustomFieldValueGetIn input) {
		LogicUtils.assertNotNull(input.getObjectType(), "objectType");
		LogicUtils.assertNotEmpty(input.getObjectId(), "objectId");

		BooleanBuilder filter = new BooleanBuilder()//
				.and(Qs.customField.objectType.eq(input.getObjectType()))//
				.and(Qs.customField.defaultField.eq(false))//
				.and(Qs.customField.deleted.eq(false));
		if (!input.isIncludeHiddenFields()) {
			filter.and(Qs.customField.hidden.eq(false));
		}
		QueryUtils.eq(filter, Qs.customField.propertyGroup, input.getPropertyGroup());

		Map<Long, Map<Long, FieldValue>> fields = new LinkedHashMap<>();
		{
			List<FieldValue> list = BeanUtils.get(JPAQueryFactory.class)//
					.select(Projections.constructor(FieldValue.class, //
							Qs.customField.id, //
							Qs.customField.fieldName, //
							Qs.customField.propertyGroup, //
							Qs.customField.fieldType//
					))//
					.from(Qs.customField)//
					.where(filter)//
					.orderBy(//
							Qs.customField.propertyGroup.asc(), //
							Qs.customField.position.asc(), //
							Qs.customField.createdAt.asc()//
					)//
					.fetch();
			input.getObjectId().forEach(objectId -> {
				Map<Long, FieldValue> map = new LinkedHashMap<>();
				list.forEach(item -> map.put(item.getFieldId(), ValueUtils.map(item, FieldValue.class)));
				fields.put(objectId, map);
			});
		}

		BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FieldValue.class, //
						Qs.customFieldValue.objectId, //
						Qs.customField.id, //
						Qs.customField.fieldName, //
						Qs.customField.propertyGroup, //
						Qs.customField.fieldType, //
						Qs.customFieldValue.value//
				))//
				.from(Qs.customField)//
				.innerJoin(Qs.customFieldValue).on(//
						Qs.customFieldValue.customField.eq(Qs.customField)//
								.and(Qs.customFieldValue.objectId.in(input.getObjectId()))//
								.and(Qs.customFieldValue.value.isNotNull())//
				)//
				.where(filter)//
				.orderBy(Qs.customFieldValue.objectId.asc(), Qs.customFieldValue.id.asc())//
				.fetch()//
				.forEach(item -> fields.get(item.getObjectId()).get(item.getFieldId()).addValues(item.getValues()));

		Map<Long, List<FieldValue>> output = new LinkedHashMap<>();
		fields.forEach((objectId, map) -> output.put(objectId, new ArrayList<>(map.values())));
		return output;
	}

}
