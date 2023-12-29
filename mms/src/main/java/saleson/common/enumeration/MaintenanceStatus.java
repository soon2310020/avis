package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MaintenanceStatus implements CodeMapperType {
	UPCOMING("Upcoming"),
	OVERDUE("Overdue"),
	SENT("Sent"),
	DONE("Done");

	private String title;

	MaintenanceStatus(String title) {
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
