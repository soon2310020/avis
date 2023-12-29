package com.emoldino.api.integration.resource.composite.pqwrk.dto;

@Deprecated
public enum PpaStatus {
	WARM_UP("WARM_UP"), COOL_DOWN("COOL_DOWN"), STABLE("STABLE"), NO_PRODUCTION("NO_PRODUCTION");

	PpaStatus(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}
}
