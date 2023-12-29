package com.emoldino.api.asset.resource.composite.alrrlo.dto;

import java.util.List;

import lombok.Data;

@Data
public class AlrRloGetIn {
	private String query;
	private String filterCode;

	private String tabName;
	private List<Long> id;
}
