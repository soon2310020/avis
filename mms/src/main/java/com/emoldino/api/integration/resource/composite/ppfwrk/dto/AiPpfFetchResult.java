package com.emoldino.api.integration.resource.composite.ppfwrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AiPpfFetchResult {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private int generation;
	private AiPpfFetchFields data;
}
