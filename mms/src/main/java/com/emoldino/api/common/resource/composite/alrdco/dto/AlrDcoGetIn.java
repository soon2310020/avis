package com.emoldino.api.common.resource.composite.alrdco.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlrDcoGetIn {
	private String query;
	private String filterCode;
	private String tabName;
	private List<Long> id;
}
