package com.emoldino.api.common.resource.base.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.PropertyGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomFieldGetIn {
	private ObjectType objectType;
	private PropertyGroup propertyGroup;
	private String fieldName;
}
