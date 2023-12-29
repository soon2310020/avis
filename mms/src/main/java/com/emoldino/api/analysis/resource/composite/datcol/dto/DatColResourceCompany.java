package com.emoldino.api.analysis.resource.composite.datcol.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.Data;
import saleson.common.enumeration.CompanyType;

@Data
public class DatColResourceCompany {
	@Enumerated(EnumType.STRING)
	private CompanyType companyType;
	private String name;
	private String companyCode;
	private String address;
	private String manager;
	private String phone;
	private String email;
	private String memo;
	private boolean enabled;
}
