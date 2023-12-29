package com.emoldino.api.integration.resource.base.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiLaunchIn {
	private String moldId;
	private String lst;
	private String aiType;
}
