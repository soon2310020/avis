package com.emoldino.api.common.resource.composite.cfgstp.dto;

import java.util.Map;

import lombok.Data;
import saleson.common.enumeration.ConfigCategory;

@Data
public class CfgStpPostIn {
	private Map<ConfigCategory, Map<String, Object>> options;
}
