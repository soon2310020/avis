package saleson.common.enumeration.customField;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CustomFieldType implements CodeMapperType {
	DROPDOWN("Dropdown"), //
	TEXT_BOX("Text box"), //
	NUMBER("Number"), //
	DATE("Date"), //
	CHECK_BOXES("Check box"), //
	TEXT("Text");

	private String title;

	CustomFieldType(String title) {
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
