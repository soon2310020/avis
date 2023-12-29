package com.emoldino.api.integration.resource.composite.mfewrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMfeFetchResult {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private int generation;	
	private AiMfeFetchFields data;
}
