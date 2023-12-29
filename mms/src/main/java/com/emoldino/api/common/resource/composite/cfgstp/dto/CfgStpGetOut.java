package com.emoldino.api.common.resource.composite.cfgstp.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import saleson.common.enumeration.ConfigCategory;

@Data
public class CfgStpGetOut {
	private Map<ConfigCategory, Object> options = new LinkedHashMap<>();
}
