package com.emoldino.api.common.resource.composite.datimp.service.enumeration;

import lombok.Getter;

@Getter
public enum MaintenanceIntervalType {
	SHOT_BASED("Shot Based"), WEEKLY_TIME_BASED("Weekly Time Based"), MONTHLY_TIME_BASED("Monthly Time Based");

	private final String title;

	MaintenanceIntervalType(String title) { //
		this.title = title;
	}
}
