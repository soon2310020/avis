package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DateViewType implements CodeMapperType {
	HOUR("Hour"), //
	DAY("Day"), //
	WEEK("Week"), //
	MONTH("Month"), //
	YEAR("Year"), //
	LAST7DAYS("Last 7 days"), //
	LAST30DAYS("Last 30 days"),
	CUSTOM_RANGE("Custom Range"),
	EVERY_SHOT("Every Shot")
	;

	private String title;

	DateViewType(String title) {
		this.title = title;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Boolean isEnabled() {
		return true;
	}
}
