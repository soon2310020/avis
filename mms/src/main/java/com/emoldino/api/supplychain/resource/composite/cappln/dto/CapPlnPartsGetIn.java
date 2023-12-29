package com.emoldino.api.supplychain.resource.composite.cappln.dto;

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
public class CapPlnPartsGetIn extends TimeSetting {
	private String query;

	public String getFilterCode() {
		return "COMMON";
	}
}
