package com.emoldino.api.common.resource.base.option.dto;

import com.emoldino.framework.enumeration.TimeScale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InactiveConfig {
	private Long level1;
	private Long level2;
	private Long level3;
	private TimeScale timeScale;
}
