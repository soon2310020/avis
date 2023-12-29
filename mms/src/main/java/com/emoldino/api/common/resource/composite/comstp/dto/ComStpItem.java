package com.emoldino.api.common.resource.composite.comstp.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.model.Company;

@Data
@NoArgsConstructor
public class ComStpItem {
	private Long id;
	private String name;
	private String companyCode;
	private CompanyType companyType;
	private String address;
	private String manager;
	private String email;
	private String phone;
	private String memo;
	private String createdAtDate;
	private boolean enabled;

	private long moldCount;

	@DataLeakDetector(disabled = true)
	private List<FltCompany> upperTierCompanies;

	public ComStpItem(Company company, String manager, String email, String phone, String memo, Object moldCount) {
		ValueUtils.map(company, this);
		this.manager = manager;
		this.email = email;
		this.phone = phone;
		this.memo = memo;
		// TODO by user timezone
		this.createdAtDate = DateUtils2.format(company.getCreatedAt(), DateUtils2.DatePattern.yyyy_MM_dd, DateUtils2.Zone.GMT);

		this.moldCount = moldCount == null ? 0L : (long) moldCount;
	}
}
