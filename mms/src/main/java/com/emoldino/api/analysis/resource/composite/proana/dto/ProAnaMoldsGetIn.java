package com.emoldino.api.analysis.resource.composite.proana.dto;

import lombok.Data;

@Data
public class ProAnaMoldsGetIn {
	private String query;

	public String getFilterCode() {
		return "COMMON";
	}
}
