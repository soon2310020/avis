package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum IpType implements CodeMapperType {
	DYNAMIC("Dynamic"),
	STATIC("Static");

	private String title;

	IpType(String title) {
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
