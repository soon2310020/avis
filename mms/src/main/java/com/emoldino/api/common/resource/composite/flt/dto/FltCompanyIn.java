package com.emoldino.api.common.resource.composite.flt.dto;

import java.util.Arrays;
import java.util.List;

import com.emoldino.framework.util.ValueUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.CompanyType;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class FltCompanyIn extends FltIn {
	private List<CompanyType> companyType;
	private List<Long> partId;

	public FltCompanyIn(FltIn fltIn, CompanyType... companyType) {
		ValueUtils.map(fltIn, this);
		this.companyType = companyType == null ? null : Arrays.asList(companyType);
	}

	public FltCompanyIn(FltIn fltIn, List<CompanyType> companyType) {
		ValueUtils.map(fltIn, this);
		this.companyType = companyType;
	}
}
