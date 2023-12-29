package com.emoldino.api.common.resource.base.object.repository.customfield;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import saleson.common.enumeration.ObjectType;
import saleson.model.customField.CustomField;

import java.util.List;

public interface CustomFieldRepository  extends JpaRepository<CustomField, Long>, QuerydslPredicateExecutor<CustomField> {
    List<CustomField> findByObjectTypeOrderByPosition(ObjectType objectType);
    @Query("select c from CustomField c where c.objectType=:objectType and upper(c.fieldName) = upper(:fieldName)  ")
    List<CustomField> findByObjectTypeAndFieldNameEquals(@Param("objectType") ObjectType objectType,
                                                         @Param("fieldName")  String fieldName, Pageable pageable);
    List<CustomField> findByObjectTypeOrderByFieldNameAsc(ObjectType objectType);

}
