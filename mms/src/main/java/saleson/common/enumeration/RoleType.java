package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RoleType implements CodeMapperType {
	ROLE("일반"), //
	ROLE_MENU("Access Feature"), //
	ROLE_GROUP("Access Group"), //
	ROLE_CONTROL("Access Control");

	private String title;

	RoleType(String title) {
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
