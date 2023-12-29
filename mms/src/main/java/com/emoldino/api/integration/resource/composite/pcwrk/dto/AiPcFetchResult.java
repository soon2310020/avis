package com.emoldino.api.integration.resource.composite.pcwrk.dto;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AiPcFetchResult {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private int generation;
	private AiPcFetchFields data;
}
