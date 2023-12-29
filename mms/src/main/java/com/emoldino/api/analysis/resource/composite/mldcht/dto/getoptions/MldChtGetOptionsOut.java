package com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions;

import java.util.List;

import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtTimeScale;

import lombok.Data;

@Data
public class MldChtGetOptionsOut {
	private List<MldChtTimeScale> availableTimeScales;

//	private MldChtOptionsItem sc;
//	private MldChtOptionsItem act;
//	private MldChtOptionsItem wact;
//	private MldChtOptionsItem ct;
//	private MldChtOptionsItem minCt;
//	private MldChtOptionsItem maxCt;

//	@Data
//	@NoArgsConstructor
//	@JsonInclude(Include.NON_NULL)
//	public static class MldChtOptionsItem {
//		private MldChtChartType chartType;
//		private boolean selected;
//	}
}
