package com.emoldino.api.common.resource.base.accesscontrol.dto;

import lombok.Data;

@Data
public class RoleGetUsersIn {
	private Long locationId;
	private String query;
}
