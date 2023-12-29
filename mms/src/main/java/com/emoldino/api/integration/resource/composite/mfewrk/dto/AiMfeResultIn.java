package com.emoldino.api.integration.resource.composite.mfewrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMfeResultIn {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private String generation;	
	private AiMfeResultFields data;
}
