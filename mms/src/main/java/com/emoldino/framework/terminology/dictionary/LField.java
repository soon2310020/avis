package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import saleson.common.enumeration.Language;

public class LField {

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	Language language;

	@Column(length = 20)
	String locationCode;

	Long locationId;

	Instant loginAt;
	Instant logoutAt;

}
