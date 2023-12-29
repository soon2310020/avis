package com.emoldino.api.common.resource.base.object.repository.customfieldvalue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long>, QuerydslPredicateExecutor<CustomFieldValue> {

	List<CustomFieldValue> findByCustomFieldAndObjectId(CustomField customField, Long objectId);

	@Deprecated
	// TODO need to add ObjectType filter
	void deleteAllByObjectId(Long objectId);

	List<CustomFieldValue> findByCustomFieldInAndObjectIdIn(List<CustomField> customField, List<Long> objectId);

}
