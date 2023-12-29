package com.emoldino.api.integration.resource.base.ai.dto;

import java.util.Map;

import lombok.Data;

@Data
public class AiFetchSchema {
	private AiModelType aiType;
	private AiFetchInferenceBoundary inferenceBoundary;
	private AiFetchScale scale;
	private Map<String, Object> fields;
	private AiFetchTarget target;
}
