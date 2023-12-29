package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import com.emoldino.framework.dto.TimeSetting;

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
public class DemCplProductsGetIn extends TimeSetting {
	private String query;

	public String getFilterCode() {
		return "COMMON";
	}
}
