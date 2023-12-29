package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum WeightUnit implements CodeMapperType {
	OUNCES("ounce"),
	POUNDS("pound"),
	GRAMS("gram"),
	KILOGRAMS("kilogram");

	private String title;

	WeightUnit(String title) {
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
