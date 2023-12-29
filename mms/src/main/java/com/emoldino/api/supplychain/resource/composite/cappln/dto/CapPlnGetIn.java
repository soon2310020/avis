package com.emoldino.api.supplychain.resource.composite.cappln.dto;

import com.emoldino.api.supplychain.resource.composite.cappln.enumeration.CapPlnViewBy;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CapPlnGetIn extends CapPlnIn {
	@ApiModelProperty(value = "View By", required = true)
	private CapPlnViewBy viewBy;
	@ApiModelProperty(value = "Product ID / Part ID (Depending on the 'View By' value)", required = true, example = "1")
	private Long id;
}
