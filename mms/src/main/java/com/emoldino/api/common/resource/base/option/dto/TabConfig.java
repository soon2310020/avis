package com.emoldino.api.common.resource.base.option.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TabConfig {
	private boolean defaultTab = true;
	private Map<String, Object> input;

	public TabConfig(boolean defaultTab) {
		super();
		this.defaultTab = defaultTab;
	}
}
