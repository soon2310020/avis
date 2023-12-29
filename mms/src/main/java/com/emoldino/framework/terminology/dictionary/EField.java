package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Lob;

import saleson.common.hibernate.converter.BooleanYnConverter;

public class EField {

	@Column(length = 1000)
	String emailTitle;

	@Lob
	String emailContent;

	@Convert(converter = BooleanYnConverter.class)
	@Column(length = 1)
	boolean enabled;

	Instant expiredAt;

}
