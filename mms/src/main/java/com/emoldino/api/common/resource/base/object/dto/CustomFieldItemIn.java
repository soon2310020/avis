package com.emoldino.api.common.resource.base.object.dto;

import lombok.Data;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.PropertyGroup;

@Data
public class CustomFieldItemIn {
	private ObjectType objectType;
	private PropertyGroup propertyGroup;
	private String fieldName;

	private Integer position;

	private Boolean required;
	private Boolean defaultInput;
	private String defaultInputValue;

}
