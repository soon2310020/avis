package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MisconfigureStatus implements CodeMapperType {
	MISCONFIGURED("Misconfigured"),
	CONFIRMED("Confirmed"),
	CANCELED("Canceled");

	private String title;

	MisconfigureStatus(String title) {
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
