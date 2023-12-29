package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum PeriodType implements CodeMapperType {
	THIS_MONTH("This month", true),
	RECENT_MONTH("Recent 1 month", true),
	THIS_YEAR("This year", true),
	RECENT_YEAR("Recent 1 year", true),
	DAILY("Daily", true),
	WEEKLY("Weekly", true),
	MONTHLY("Monthly", true),
	REAL_TIME("Real Time", true);


	private String title;
	private boolean enabled;

	PeriodType(String title, boolean enabled) {
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
		return enabled;
	}


}
