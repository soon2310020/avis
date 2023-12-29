package com.emoldino.api.asset.resource.composite.wgttolrlo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WgtTolRloGetOut {
	private String title;
	private BigDecimal value;
	private String unit;
	private String title1;
	private BigDecimal value1;
	private String detail1;
	private String title2;
	private BigDecimal value2;
	private String detail2;
	private String title3;
	private BigDecimal value3;
	private String detail3;
}
