package com.emoldino.api.common.resource.composite.usrviw.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;

@Deprecated
@Data
@NoArgsConstructor
public class UsrViwUserItem {

	public UsrViwUserItem(Long id, String name, Long companyId) {
		this(id, name, companyId, null, null, null);
	}

	public UsrViwUserItem(Long id, String name, Long companyId, CompanyType companyType, String companyCode, String companyName) {
		this.id = id;
		this.name = name;
		this.companyId = companyId;
		this.companyCode = companyCode;
		this.companyName = companyName;
		this.companyType = companyType;
	}

	@ApiModelProperty(value = "ID")
	private Long id;
	@ApiModelProperty(value = "Name of This User")
	private String name;
	@ApiModelProperty(value = "Company ID of This User")
	private Long companyId;
	@ApiModelProperty(value = "Company Type of This User")
	private CompanyType companyType;
	@ApiModelProperty(value = "Company Code of This User")
	private String companyCode;
	@ApiModelProperty(value = "Company Name of This User")
	private String companyName;
}
