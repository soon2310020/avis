package com.emoldino.api.integration.resource.composite.pqwrk.dto;

@Deprecated
public enum TempStatus {
	INCREASE("INCREASE"), DECREASE("DECREASE"), STABLE("STABLE");

	TempStatus(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}
}
