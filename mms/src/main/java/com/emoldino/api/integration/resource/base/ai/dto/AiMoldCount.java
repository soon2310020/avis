package com.emoldino.api.integration.resource.base.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiMoldCount {
	private Long moldId;
	private Long count;
}
