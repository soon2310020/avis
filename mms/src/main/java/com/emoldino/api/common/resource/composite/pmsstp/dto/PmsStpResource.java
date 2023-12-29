package com.emoldino.api.common.resource.composite.pmsstp.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PmsStpResource {
	private String resourceId;
	private String name;
	private Map<String, Object> resourceFields = new LinkedHashMap<>();

	public void setFieldValue(String name, Object value) {
		if (value == null) {
			if (resourceFields.containsKey(name)) {
				resourceFields.remove(name);
			}
			return;
		}
		resourceFields.put(name, value);
	}
}
