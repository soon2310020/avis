package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum ToolingCondition implements CodeMapperType {
	GOOD("Good"),
	FAIR("Fair"),
	POOR("Poor");

	private String title;

	ToolingCondition(String title) {
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
