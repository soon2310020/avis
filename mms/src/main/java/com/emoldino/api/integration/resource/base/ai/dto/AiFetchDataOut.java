package com.emoldino.api.integration.resource.base.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AiFetchDataOut {
	private String aiType;
	private String moldId;
	private String lst;
	private AiFetchLong shotCount;
	private AiFetchLong cycleTime;
	private AiFetchLong temperature;
	@JsonProperty("LST")
	private AiFetchString lstValue;
	@JsonProperty("RT")
	private AiFetchString rtValue;

	@Data
	@NoArgsConstructor
	@JsonInclude(Include.NON_NULL)
	public static class AiFetchLong {
		private List<Long> hourly;
		private List<Long> daily;
		private List<Long> weekly;
		private List<Long> monthly;
		private List<Long> yearly;
	}

	@Data
	@NoArgsConstructor
	@JsonInclude(Include.NON_NULL)
	public static class AiFetchString {
		private List<String> hourly;
		private List<String> daily;
		private List<String> weekly;
		private List<String> monthly;
		private List<String> yearly;
	}
}
