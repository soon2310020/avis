package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiProcStatus;

public class TField {

	Integer targetUptimeRate;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiProcStatus taskStatus;

	@Column(length = 1000)
	String title;

}
