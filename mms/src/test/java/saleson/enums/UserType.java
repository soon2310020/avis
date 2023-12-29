package saleson.enums;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum UserType implements CodeMapperType {
	SYSTEM("시스템관리자"),
	HQ("Headquarter"),
	OUTSOURCING("금형 외주 제작사"),
	SUPPLIER("부품 제조 협력사");

	private String title;

	UserType(String title) {
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
