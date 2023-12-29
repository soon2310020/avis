package com.emoldino.api.integration.resource.composite.mfewrk.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMfeFetchFields {
	private Long moldId;
	private List<String> tff;
	private List<String> hour;
	private List<Integer> shotCount;
	private List<Integer> tav;
	private List<Integer> cycleTime;
}
