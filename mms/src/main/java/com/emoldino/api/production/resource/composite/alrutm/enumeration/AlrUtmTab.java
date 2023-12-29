package com.emoldino.api.production.resource.composite.alrutm.enumeration;

import lombok.Getter;

@Getter
public enum AlrUtmTab {
	OUTSIDE_L1("Outside L1"), //
	OUTSIDE_L2("Outside L2");

	private final String title;

	AlrUtmTab(String title) {
		this.title = title;
	}
}
