package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum DataRequestStatus implements CodeMapperType {

	ACCEPTED("Accepted"),
	REQUESTED("Requested"),
	DECLINED("Declined"),
	CANCELLED("Cancelled"),
	COMPLETED("Completed"),
	;


	private String title;

	DataRequestStatus(String title) {
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
