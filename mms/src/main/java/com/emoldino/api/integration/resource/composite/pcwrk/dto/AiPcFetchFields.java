package com.emoldino.api.integration.resource.composite.pcwrk.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AiPcFetchFields {
	private long moldId;
	private String counterId;
	private int contractedCycleTime;
	private String startHour;
	private List<Long> dataAccId;
	private List<String> measurementDate;
	private List<String> accelerations;
	private List<Double> similarityMetric;
	private List<Double> similarityMetricHr;
	private List<Integer> procChanged; // 1: process change observed, 0: no process change observed
}
