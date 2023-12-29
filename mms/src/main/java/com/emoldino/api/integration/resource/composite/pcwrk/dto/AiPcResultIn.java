package com.emoldino.api.integration.resource.composite.pcwrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.Data;

@Data
public class AiPcResultIn {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private AiPcResultFields data;
}
