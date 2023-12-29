package com.emoldino.api.integration.resource.composite.tsdwrk.dto;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTsdResultFields {
	private Long moldId;
	private int low_sc_threshold;
	private int high_sc_threshold;
	
	private int needsRelabelling; // 0, 1
	@Enumerated(EnumType.STRING)
	private TsdLabel newLabel;
	
	private List<String> statisticsId;
	private List<String> hour;
	private List<Integer> shotCount;
	private List<Integer> correctedShotCount;
	@Enumerated(EnumType.STRING)
	private List<TsdLabel> tsdLabel;
	private List<Integer> isMissing;
}
