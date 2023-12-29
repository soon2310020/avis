package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CustomerSupportStatus implements CodeMapperType {
	ALL("All"),
	NEW("New"),
	IN_PROGRESS("In Progress"),
	RESOLVED("Resolved")
	;

	private String title;

	CustomerSupportStatus(String title) {
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
