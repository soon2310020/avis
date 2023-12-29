package com.emoldino.api.analysis.resource.composite.toldat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TolDatGetIn {
	private Long moldId;
	private String moldCode;
	private boolean mock;
}
