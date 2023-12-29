package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;

public class WField {

	@Column(length = 6)
	String week;

	Long weeklyCapa;

	Double weightedAvgCycleTime;

}
