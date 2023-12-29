package com.emoldino.api.asset.resource.base.mold.dto;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;

import lombok.Data;

@Data
public class ToolingStatusSummaryGetIn {
	private String filterCode;
	private ToolingUtilizationStatus utilizationStatus;
}
