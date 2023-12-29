package com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WgtCycTimCplGetOut {
	private String title;
	private String title1;
	@JsonIgnore
	private long value1Sc;
	private String title2;
	@JsonIgnore
	private long value2Sc;
	private String title3;
	@JsonIgnore
	private long value3Sc;

	@QueryProjection
	public WgtCycTimCplGetOut(long value1Sc, long value2Sc, long value3Sc) {
		this.value1Sc = value1Sc;
		this.value2Sc = value2Sc;
		this.value3Sc = value3Sc;
	}

	public String getUnit() {
		return "%";
	}

	public long getValue() {
		return getValue1();
	}

	public long getValue1() {
		return getTotalSc() == 0 ? 0 : (100 - (getValue2() + getValue3()));
	}

	public long getValue2() {
		return getTotalSc() == 0d ? 0 : ValueUtils.toLong(value2Sc / getTotalSc() * 100d, 0L);
	}

	public long getValue3() {
		return getTotalSc() == 0d ? 0 : ValueUtils.toLong(value3Sc / getTotalSc() * 100d, 0L);
	}

	private double getTotalSc() {
		return value1Sc + value2Sc + value3Sc;
	}
}
