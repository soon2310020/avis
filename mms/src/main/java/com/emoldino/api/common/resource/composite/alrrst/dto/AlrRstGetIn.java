package com.emoldino.api.common.resource.composite.alrrst.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlrRstGetIn {
	private String query;
	private String filterCode;
	private String tabName;
	private List<Long> id;
}
