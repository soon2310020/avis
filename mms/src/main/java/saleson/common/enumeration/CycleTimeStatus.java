package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CycleTimeStatus implements CodeMapperType {
	WITHIN_TOLERANCE("Within Tolerance"),
	OUTSIDE_L1("Outside L1"),
	OUTSIDE_L2("Outside L2");

	private String title;

	CycleTimeStatus(String title) {
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
