package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CompanyType implements CodeMapperType {
	IN_HOUSE("In-house", true), //
	SUPPLIER("Supplier", true), //
	TOOL_MAKER("Toolmaker", true);

	private String title;
	private Boolean enabled;

	CompanyType(String title, boolean enabled) {
		this.title = title;
		this.enabled = enabled;

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
