package com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions;

import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtDataGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MldChtGetOptionsIn {
	@ApiModelProperty(value = "Mold ID", required = true, example = "1")
	private Long moldId;
	@ApiModelProperty(value = "Data Group", required = true)
	private MldChtDataGroup dataGroup;
}
