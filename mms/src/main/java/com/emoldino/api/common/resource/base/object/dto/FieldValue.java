package com.emoldino.api.common.resource.base.object.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.customField.CustomFieldType;
import saleson.common.enumeration.customField.PropertyGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldValue {
	@JsonIgnore
	private Long objectId;
	@JsonIgnore
	private Long fieldId;
	private String fieldName;
	private PropertyGroup propertyGroup;
	private String dataType;
	private List<String> values;

	public FieldValue(Long fieldId, String fieldName, PropertyGroup propertyGroup, CustomFieldType dataType) {
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.propertyGroup = propertyGroup;
		this.dataType = dataType == null ? null : dataType.name();
		this.values = new ArrayList<>();
	}

	public FieldValue(Long objectId, Long fieldId, String fieldName, PropertyGroup propertyGroup, CustomFieldType dataType, String value) {
		this.objectId = objectId;
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.propertyGroup = propertyGroup;
		this.dataType = dataType == null ? null : dataType.name();
		this.values = value == null ? new ArrayList<>() : Arrays.asList(value);
	}

	public String getValue() {
		return ObjectUtils.isEmpty(values) ? null : values.get(0);
	}

	public void addValue(String value) {
		if (this.values == null) {
			this.values = new ArrayList<>();
		}
		this.values.add(value);
	}

	public void addValues(List<String> values) {
		if (ObjectUtils.isEmpty(values)) {
			return;
		}
		if (this.values == null) {
			this.values = new ArrayList<>();
		}
		this.values.addAll(values);
	}
}
