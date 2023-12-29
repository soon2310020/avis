package com.emoldino.api.analysis.resource.composite.prochg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProChgDetailsGetIn {
	private Long moldId;
	private String dateHourRange;

	public String getFilterCode() {
		return "COMMON";
	}
}
