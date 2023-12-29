package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;

import saleson.common.hibernate.converter.BooleanYnConverter;

public class DField {

	Long dataId;

	@Column(length = 255)
	String dataType;

	@Column(length = 8)
	String day;

	@Convert(converter = BooleanYnConverter.class)
	@Column(length = 1)
	boolean deleted;

	Instant deletedAt;

	Integer downtimeDuration;

}
