package com.emoldino.api.integration.resource.composite.pqwrk.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AiPqFetchFields {
	private Long moldId;
	private Double wact;
	private String startHour;
	private List<Long> statisticsId;		
	private List<Integer> shotCount;
	private List<Double> cycleTime;
	private List<Integer> temperature;
	private List<String> hour;
	private List<String> tff;
}
