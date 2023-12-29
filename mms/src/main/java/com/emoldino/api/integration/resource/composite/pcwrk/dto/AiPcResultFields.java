package com.emoldino.api.integration.resource.composite.pcwrk.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AiPcResultFields {
	private Long moldId;
	private String counterId;
	private List<Long> dataAccId;
	private List<Double> similarityMetric;
	private List<Double> similarityMetricHr;
	private List<Integer> procChanged; // 1: process change observed, 0: no process change observed
}
