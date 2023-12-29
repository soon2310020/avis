package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.api.common.resource.base.location.enumeration.AreaType;

public class AField {

	Long actualShotCount;

	Integer apprCycleTime;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	AreaType areaType;

	Double avgCycleTime;

}
