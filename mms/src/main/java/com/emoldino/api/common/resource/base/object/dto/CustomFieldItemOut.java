package com.emoldino.api.common.resource.base.object.dto;

import lombok.Data;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.PropertyGroup;

@Data
public class CustomFieldItemOut {
	private Long id;

	private ObjectType objectType;
	private PropertyGroup propertyGroup;
	private String fieldName;

	private Boolean required;
	private Boolean defaultInput;
	private String defaultInputValue;
}
