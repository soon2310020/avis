package com.emoldino.framework.enumeration;

import lombok.Getter;

@Getter
public enum AlertTab {
	ALERT("Alert"), //
	HISTORY_LOG("History Log");

	private final String title;

	AlertTab(String title) {
		this.title = title;
	}
}
