package com.emoldino.api.common.resource.base.accesscontrol.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionDto {
	private String resourceId;

	public PermissionDto(String resourceId) {
		this.resourceId = resourceId;
	}
}
