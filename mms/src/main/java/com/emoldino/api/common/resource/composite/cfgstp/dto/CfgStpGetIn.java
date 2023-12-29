package com.emoldino.api.common.resource.composite.cfgstp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import saleson.common.enumeration.ConfigCategory;

@Data
public class CfgStpGetIn {
	private List<ConfigCategory> configCategory = new ArrayList<>();
}
