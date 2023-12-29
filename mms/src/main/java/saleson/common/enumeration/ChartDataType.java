package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ChartDataType implements CodeMapperType {
	QUANTITY("Quantity"), //
	MAXIMUM_CAPACITY("Maximum Capacity"), //
	CYCLE_TIME("Cycle Time"), //
	APPROVED_CYCLE_TIME("Approved Cycle Time"), //
	UPTIME("Uptime"), //
	CYCLE_TIME_ANALYSIS("Cycle Time Analysis"), //
	TEMPERATURE_ANALYSIS("Temperature Analysis"), //
	CYCLE_TIME_DASHBOARD("Cycle Time Dashboard");

	private String title;

	ChartDataType(String title) {
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
