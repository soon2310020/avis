package com.emoldino.api.common.resource.composite.alrdatapr.dto;

import java.util.List;

import lombok.Data;

@Data
public class AlrDatAprGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	private List<Long> id;
}
