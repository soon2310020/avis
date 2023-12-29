package com.emoldino.api.common.resource.composite.alrdet.dto;

import java.util.List;

import lombok.Data;

@Data
public class AlrDetGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	private List<Long> id;
}
