package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MoldLocationStatus implements CodeMapperType {
	CHANGED("위치변경"), //
	PENDING("PENDING"), //
	APPROVED("APPROVED"), //
	DISAPPROVED("DISAPPROVED"), //
	@Deprecated
	UNAPPROVED("UNAPPROVED"), //
	CONFIRMED("확인완료");

	private String title;

	MoldLocationStatus(String title) {
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
