package com.emoldino.api.integration.resource.composite.tsdwrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.Data;

@Data
public class AiTsdResultIn {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private int generation;	
	private AiTsdResultFields data;
}
