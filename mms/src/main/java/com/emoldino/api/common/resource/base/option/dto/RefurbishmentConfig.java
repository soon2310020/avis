package com.emoldino.api.common.resource.base.option.dto;

import lombok.Data;
import saleson.common.enumeration.ConfigOption;

@Data
public class RefurbishmentConfig {
	private boolean enabled;
	private ConfigOption configOption;
	// Medium - High
	private Double mh = 80d;
	// Low - Medium
	private Double lm = 50d;
	private Double hmMonths = 8d;
	private Double mlMonths = 12d;

	public boolean isEnabled() {
		return true;
	}
}
