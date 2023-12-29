package com.emoldino.api.common.resource.composite.manpag.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import saleson.common.enumeration.ConfigCategory;

@Data
public class ManPagGetOptionsOut {
	private Map<ConfigCategory, Map<String, Object>> options = new LinkedHashMap<>();
}
