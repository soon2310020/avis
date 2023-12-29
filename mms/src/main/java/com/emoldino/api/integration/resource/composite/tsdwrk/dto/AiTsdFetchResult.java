package com.emoldino.api.integration.resource.composite.tsdwrk.dto;

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
public class AiTsdFetchResult {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;	
	private AiTsdFetchFields data;
}
