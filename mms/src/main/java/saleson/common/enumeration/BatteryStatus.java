package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum BatteryStatus implements CodeMapperType {
	HIGH("High"),
	LOW("Low"),
	EMPTY("Empty");

	private String title;

	BatteryStatus(String title) {
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
