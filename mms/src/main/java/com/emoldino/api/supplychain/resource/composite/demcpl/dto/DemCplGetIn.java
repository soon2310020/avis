package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import com.emoldino.api.supplychain.resource.composite.demcpl.enumeration.DemCplViewBy;
import com.emoldino.framework.dto.TimeSetting;

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
public class DemCplGetIn extends TimeSetting {
	@ApiModelProperty(value = "View By", required = true)
	private DemCplViewBy viewBy;
	@ApiModelProperty(value = "Product ID / Part ID (Depending on the 'View By' value)", required = true, example = "1")
	private Long id;

	public String getFilterCode() {
		return "COMMON";
	}
}
