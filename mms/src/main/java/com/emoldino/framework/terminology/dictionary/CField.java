package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import com.emoldino.framework.enumeration.ClientType;

import saleson.common.enumeration.ConfigCategory;

public class CField {

	Integer cavityCount;

	@Column(length = 100)
	String clientId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	ClientType clientType;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	ConfigCategory configCategory;

	@Lob
	String content;

	@CreatedDate
	@Column(updatable = false)
	Instant createdAt;

	@CreatedBy
	@Column(updatable = false)
	Long createdBy;

}
