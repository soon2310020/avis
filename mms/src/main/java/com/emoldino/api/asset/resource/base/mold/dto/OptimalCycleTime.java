package com.emoldino.api.asset.resource.base.mold.dto;

import lombok.Data;

@Data
public class OptimalCycleTime {
	private String strategy;
	private int periodMonths;
	private double value;
}
