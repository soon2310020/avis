package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CorrectiveStatus implements CodeMapperType {
	FAILURE("Failure"),
	REQUESTED("Requested"),
	COMPLETED("Completed"),
	APPROVED("Approved"),
	DISAPPROVED("Disapproved"),
	REPAIRED("Repaired");

	private String title;

	CorrectiveStatus(String title) {
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
