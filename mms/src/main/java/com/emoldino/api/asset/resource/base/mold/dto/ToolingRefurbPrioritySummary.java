package com.emoldino.api.asset.resource.base.mold.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolingRefurbPrioritySummary {
	private long high;
	private long medium;
	private long low;
}
