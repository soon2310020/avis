package com.emoldino.api.integration.resource.composite.ppfwrk.dto;

import java.util.List;

import com.emoldino.api.integration.resource.composite.tsdwrk.dto.TsdLabel;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AiPpfFetchFields {
	private long moldId;
	private int cavityNumber; // SUM(MOLD_PART.CAVITY)
	private int designedShot; // MOLD.DESIGNED_SHOT
	private List<Long> statisticsId; // STATISTICS.ID
	private List<String> hour; // STATISTICS.HOUR
	private List<String> tff; // STATISTICS.TFF
	private List<Integer> shotCount; // AI_TSD_RESULT.SHOT_COUNT
	private List<Integer> correctedShotCount; // AI_TSD_RESULT.CORRECTED_SHOT_COUNT
	private List<Integer> sensorCumulativeShotCount; // CDATA.SC
	private TsdLabel lastLabel; // AI_TSD_RESULT.TSD_LABEL
}
