package com.emoldino.api.common.resource.base.version.dto;

import lombok.Data;

@Data
public class AppVersionGetPageIn {
	private String appCode;
	private String version;
	private Boolean enabled;
	private String updatedAtStrGt;
}
