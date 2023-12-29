package com.emoldino.api.analysis.resource.composite.ovrutl.dto;

import lombok.Data;

@Data
public class OvrUtlDetailsGetIn {
	private Long supplierId;

	public String getFilterCode() {
		return "COMMON";
	}

}
