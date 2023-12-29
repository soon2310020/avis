package com.emoldino.api.integration.resource.composite.tsdwrk.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTsdFetchFields {
	private Long moldId;
	private List<Long> statisticsId;
	private List<String> tff;
	private List<String> hour;
	private List<Integer> shotCount;
	private List<Integer> cycleTime;
	private List<Integer> tav;
	private String startDate;
	private TsdLabel lastLabel;
	private int sc_min;
	private int sc_5_perc;
	private int sc_10_perc;
	private int sc_15_perc;
	private int sc_20_perc;
	private int sc_25_perc;
	private int sc_50_perc;
	private int sc_75_perc;
	private int sc_max;

}
