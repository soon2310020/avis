package com.emoldino.api.integration.resource.composite.mfewrk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMfeResultFields {
	private Long moldId;
	private int sc_minimum;
	private int sc_5_percentile;
	private int sc_10_percentile;
	private int sc_15_percentile;
	private int sc_20_percentile;
	private int sc_25_percentile;
	private int sc_50_percentile;
	private int sc_75_percentile;
	private int sc_maximum;
	private int sc_mode;	
	private int sc_mean;
	private int uptime_mean;
	private int uptime_mode;
	private int uptime_count;
	private String mold_class;
}
