package com.emoldino.api.common.resource.base.accesscontrol.dto;

import lombok.Data;

@Data
public class RoleGetIn {
	private Boolean enabled;
	private String query;
	private Long companyId;
}
