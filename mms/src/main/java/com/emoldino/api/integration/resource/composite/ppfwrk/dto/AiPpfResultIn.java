package com.emoldino.api.integration.resource.composite.ppfwrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;

import lombok.Data;

@Data
public class AiPpfResultIn {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private long moldId;
	private AiPpfResultFields data;
}
