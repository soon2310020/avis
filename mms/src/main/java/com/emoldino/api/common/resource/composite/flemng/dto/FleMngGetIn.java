package com.emoldino.api.common.resource.composite.flemng.dto;

import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;

import lombok.Data;

@Data
public class FleMngGetIn {
	private String query;
	private Boolean enabled;
	private String fileGroupCode;
	private FileGroupType fileGroupType;
	private boolean groupByCode = true;
}
