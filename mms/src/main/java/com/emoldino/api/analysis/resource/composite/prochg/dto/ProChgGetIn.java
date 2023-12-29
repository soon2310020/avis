package com.emoldino.api.analysis.resource.composite.prochg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProChgGetIn {
	private Long moldId;
	private String month;
	private String day;

	public String getFilterCode() {
		return "COMMON";
	}
}
