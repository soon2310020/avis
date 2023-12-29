package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Company;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FltToolmaker {
	private Long id;
	private String name;
	private String companyCode;

	public FltToolmaker(Company supplier) {
		ValueUtils.map(supplier, this);
	}

	public String getCode() {
		return companyCode;
	}
}
