package com.emoldino.api.common.resource.base.license.dto;

import java.util.List;

import lombok.Data;

@Data
public class License {
	private List<String> excludeMenus;
}
