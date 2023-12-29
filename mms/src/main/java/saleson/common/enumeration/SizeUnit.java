package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum SizeUnit implements CodeMapperType {
	INCH("inch"),
	FOOT("foot"),
	YARD("yard"),
	MM("mm"),
	CM("cm"),
	M("m");

	private String title;

	SizeUnit(String title) {
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
