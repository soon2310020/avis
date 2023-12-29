package com.emoldino.api.common.resource.composite.alrdatapr.enumeration;

import lombok.Getter;

@Getter
public enum DatAprTab {
	APPROVED("Approved"), //
	DISAPPROVED("Disapproved");

	private final String title;

	DatAprTab(String title) {
		this.title = title;
	}
}
