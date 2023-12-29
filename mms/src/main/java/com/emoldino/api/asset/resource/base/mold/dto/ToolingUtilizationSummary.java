package com.emoldino.api.asset.resource.base.mold.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolingUtilizationSummary {
	private long low;
	private long medium;
	private long high;
	private long prolonged;
}
