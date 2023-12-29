package com.emoldino.api.analysis.resource.composite.datcol.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatColPostItem {
	private String deviceId;
	private String deviceType;
	private String deviceSwVersion;
	private String deviceTime;
	private String dataType;
	private String data;
	private int lastCommandIndexNo;
}
