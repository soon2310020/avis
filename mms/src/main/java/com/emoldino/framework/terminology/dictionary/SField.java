package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;

public class SField {

	@Column(length = 10)
	String schedDayOfWeek;

	Integer schedInterval;

	Integer schedOrdinalNum;

	@Column(length = 8)
	String schedStartDate;

	Integer schedUpcomingTolerance;

	@Column(length = 100)
	String sessionId;

}
