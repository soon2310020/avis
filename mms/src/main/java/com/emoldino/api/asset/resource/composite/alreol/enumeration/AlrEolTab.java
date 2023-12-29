package com.emoldino.api.asset.resource.composite.alreol.enumeration;

import lombok.Getter;

@Getter
public enum AlrEolTab {
	APPROVED("Approved"), //
	DISAPPROVED("Disapproved");

	private final String title;

	AlrEolTab(String title) {
		this.title = title;
	}
}
