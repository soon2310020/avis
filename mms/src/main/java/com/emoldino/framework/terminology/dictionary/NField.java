package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;

import saleson.common.enumeration.PriorityType;

public class NField {

	@Column(length = 100)
	String name;

	@Column(length = 8)
	String nextSchedDate;

	@Column(length = 8)
	String nextUpcomingToleranceDate;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiCategory notiCategory;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	NotiCode notiCode;

	Long notiId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiStatus notiStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	PriorityType notiPriority;

}
