package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum NotificationStatus implements CodeMapperType {
	ALERT("Alert"),
	CONFIRMED("Confirmed"),
	FIXED("Fixed"),
	PENDING("Pending"),
	APPROVED("Approved"),
	DISAPPROVED("Disapproved");

	private String title;

	NotificationStatus(String title) {
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
