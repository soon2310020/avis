package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParQuaRskGetIn {
	private String query;

	public String getFilterCode() {
		return "COMMON";
	}
}
