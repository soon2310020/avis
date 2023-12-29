package com.emoldino.api.asset.resource.composite.wgttolina.dto;

import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;

import lombok.Data;

@Data
public class WgtTolInaGetOut {

	private String title1;
	private long value1;
	private String level1;

	private String title2;
	private long value2;
	private String level2;

	private String title3;
	private long value3;
	private String level3;

	private InactiveConfig config;

}
