package com.emoldino.api.analysis.resource.composite.trdsen.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TrdSenAccelerationChartGetIn {
	@ApiModelProperty(value = "Mold ID", required = true, position = 1, example = "1")
	private Long moldId;
	@ApiModelProperty(value = "From Date - Patterns: yyyy, yyyyMM, yyyyMMdd, yyyyMMddHH, yyyyMMddHHmm, yyyyMMddHHmmss", required = false, position = 2)
	private String fromDate;
	@ApiModelProperty(value = "To Date - Patterns: yyyy, yyyyMM, yyyyMMdd, yyyyMMddHH, yyyyMMddHHmm, yyyyMMddHHmmss", required = false, position = 3)
	private String toDate;

	@Deprecated
	public String getFromDateStr() {
		return this.fromDate;
	}

	@Deprecated
	public void setFromDateStr(String fromDateStr) {
		this.fromDate = fromDateStr;
	}

	@Deprecated
	public String getToDateStr() {
		return this.toDate;
	}

	@Deprecated
	public void setToDateStr(String toDateStr) {
		this.toDate = toDateStr;
	}
}
