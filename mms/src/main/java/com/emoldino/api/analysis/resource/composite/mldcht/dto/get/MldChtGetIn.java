package com.emoldino.api.analysis.resource.composite.mldcht.dto.get;

import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtDataGroup;
import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtTimeScale;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MldChtGetIn {
	@ApiModelProperty(value = "Mold ID", required = true, position = 1, example = "1")
	private Long moldId;
	@ApiModelProperty(value = "Data Group", required = true, position = 2)
	private MldChtDataGroup dataGroup;
	@ApiModelProperty(value = "Time Scale", required = true, position = 3)
	private MldChtTimeScale timeScale;
	@ApiModelProperty(value = "From Date - Patterns: yyyy, yyyyMM, yyyyMMdd, yyyyMMddHH, yyyyMMddHHmm, yyyyMMddHHmmss", required = true, position = 4)
	private String fromDate;
	@ApiModelProperty(value = "To Date - Patterns: yyyy, yyyyMM, yyyyMMdd, yyyyMMddHH, yyyyMMddHHmm, yyyyMMddHHmmss", required = true, position = 5)
	private String toDate;
}
