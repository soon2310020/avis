package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum EquipmentType implements CodeMapperType {
	TERMINAL("Terminal"),
	COUNTER("Counter"),
	MOLD("Tooling");

	private String title;

	EquipmentType(String title) {
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
