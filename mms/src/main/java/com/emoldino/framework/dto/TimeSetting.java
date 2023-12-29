package com.emoldino.framework.dto;

import com.emoldino.framework.enumeration.TimeScale;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TimeSetting {
	@ApiModelProperty(value = "Time Scale", required = true)
	private TimeScale timeScale;
	@ApiModelProperty(value = "Time Value - Patterns: yyyyMMdd, YYYYww, yyyyMM, yyyy, yyyyMMdd-yyyyMMdd")
	private String timeValue;
	@ApiModelProperty(value = "From Date - Patterns: yyyyMMdd")
	private String fromDate;
	@ApiModelProperty(value = "To Date - Patterns: yyyyMMdd")
	private String toDate;
}
