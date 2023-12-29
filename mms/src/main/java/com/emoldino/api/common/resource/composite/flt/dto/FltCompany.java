package com.emoldino.api.common.resource.composite.flt.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.model.Company;

@Data
@NoArgsConstructor
public class FltCompany {
	private Long id;
	private String name;
	private String companyCode;
	@Enumerated(EnumType.STRING)
	private CompanyType companyType;

	public String getCode() {
		return companyCode;
	}

	public String getCompanyTypeText() {
		return this.companyType != null ? this.companyType.getTitle() : null;
	}

	public FltCompany(Company company) {
		ValueUtils.map(company, this);
	}
}
