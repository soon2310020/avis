package com.emoldino.api.integration.resource.base.ai.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiFetchDataIn {
	private String aiType;

	private List<String> dataType;
	private AiFetchPeriod period;
	private AiFetchScale scale;

	private String moldId;

	@Data
	@NoArgsConstructor
	public static class AiFetchPeriod {
		private String startTime;
		private String endTime;
	}

	@Data
	@NoArgsConstructor
	public static class AiFetchScale {
		private boolean hourly;
		private boolean daily;
		private boolean weekly;
		private boolean monthly;
		private boolean yearly;
	}
}
