package com.emoldino.api.asset.resource.composite.wgttoleol.dto;

import com.emoldino.api.common.resource.base.option.dto.RefurbPriorityConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WgtTolEolGetOut {

	private String title1;
	private long value1;

	private String title2;
	private long value2;

	private RefurbPriorityConfig config;

}
