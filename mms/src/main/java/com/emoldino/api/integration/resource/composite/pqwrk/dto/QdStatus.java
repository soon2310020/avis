package com.emoldino.api.integration.resource.composite.pqwrk.dto;

@Deprecated
public enum QdStatus {
	GOOD("GOOD"), BAD("BAD"), NO_PRODUCTION("NO_PRODUCTION"), PRESET_DISCONNECTION("PRESET_DISCONNECTION");

	QdStatus(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}
}
