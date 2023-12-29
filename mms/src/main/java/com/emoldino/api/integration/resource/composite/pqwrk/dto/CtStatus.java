package com.emoldino.api.integration.resource.composite.pqwrk.dto;

@Deprecated
public enum CtStatus {
	INCREASE("INCREASE"), DECREASE("DECREASE"), STABLE("STABLE"), VARIANCE("VARIANCE"), NO_COUNT("NO_COUNT");

	CtStatus(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}
}
