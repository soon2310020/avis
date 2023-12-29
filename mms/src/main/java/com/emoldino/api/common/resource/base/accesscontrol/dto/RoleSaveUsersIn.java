package com.emoldino.api.common.resource.base.accesscontrol.dto;

import java.util.List;

import com.emoldino.api.common.resource.base.accesscontrol.repository.userlite.UserLite;

import lombok.Data;

@Data
public class RoleSaveUsersIn {
	private Long locationId;
	private List<UserLite> content;
}
