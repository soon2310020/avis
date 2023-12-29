package com.emoldino.api.integration.resource.composite.pqwrk.dto;

import java.util.List;
import java.util.Map;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class AiPqFetchResult {
	@Enumerated(EnumType.STRING)
	private AiModelType aiType;	
	private int generation;
	private AiPqFetchFields data;
}
