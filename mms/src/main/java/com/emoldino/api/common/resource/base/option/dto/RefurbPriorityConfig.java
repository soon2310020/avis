package com.emoldino.api.common.resource.base.option.dto;

import com.emoldino.api.common.resource.base.option.enumeration.RefurbPriorityCheckBy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefurbPriorityConfig {
	private boolean enabled;
	private RefurbPriorityCheckBy checkBy;
	private double low;
	private double medium;
}
