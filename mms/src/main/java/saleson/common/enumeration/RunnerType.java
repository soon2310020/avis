package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RunnerType implements CodeMapperType {
	HOT("Direct Hot Runner"),
	SEMI_HOT("Semi Hot Runner"),
	COLD("Cold Runner");

	private String title;

	RunnerType(String title) {
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
