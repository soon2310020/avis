package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum TemperatureUnit implements CodeMapperType {
	CELSIUS("°C"),
	FAHRENHEIT("°F");

	private String title;

	TemperatureUnit(String title) {
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
