package com.emoldino.api.integration.resource.base.ai.dto;

import lombok.Data;

@Data
public class AiFetchInferenceBoundary {
	private AiFetchInferenceBoundaryType type;
	private String value;
}
