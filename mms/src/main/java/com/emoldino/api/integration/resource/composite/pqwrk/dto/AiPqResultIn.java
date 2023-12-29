package com.emoldino.api.integration.resource.composite.pqwrk.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AiPqResultIn {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;
	private int willBeSaved; // 0: Do not save, 1: Save 
	private AiPqResultFields data;
}
