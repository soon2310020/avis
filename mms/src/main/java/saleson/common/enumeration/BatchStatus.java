package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum BatchStatus implements CodeMapperType {
	PENDING("Pending"),
	IN_PROGRESS("In Progress"),
	COMPLETED("Completed")
	;

	private String title;

	BatchStatus(String title) {
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
