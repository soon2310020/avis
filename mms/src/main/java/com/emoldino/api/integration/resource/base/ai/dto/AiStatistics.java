package com.emoldino.api.integration.resource.base.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AiStatistics {
	private Long id;
	private Integer shotCountCtt;
	private Double ct;
	private Integer tav;
	private String hour;
	private String tff;
}
