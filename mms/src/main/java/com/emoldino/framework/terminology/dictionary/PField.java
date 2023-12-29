package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiProcStatus;

public class PField {

	Long partId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	PM_FREQUENCY pmFrequency;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	PM_STRATEGY pmStrategy;

	Integer position;

	@Column(length = 14)
	String procChgTime;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	NotiProcStatus procStatus;

	Integer prodDays;

	Integer prodHoursPerDay;

	Long productId;

}
