package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MessageType implements CodeMapperType {
	EMAIL("Email"),
	SMS("SMS"),
	MMS("MMS")
	;

	private String title;

	MessageType(String title) {
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
