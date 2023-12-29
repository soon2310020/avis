package com.emoldino.api.supplychain.resource.base.product.dto;

import com.emoldino.framework.enumeration.TimeScale;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDemandByTime {
	@ApiModelProperty(value = "Time Scale", required = true)
	private TimeScale timeScale;
	@ApiModelProperty(value = "Time Value - Patterns: yyyyMMdd, YYYYww, yyyyMM, yyyy, yyyyMMdd-yyyyMMdd")
	private String timeValue;
	private Long quantity;
}
