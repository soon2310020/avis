package com.emoldino.api.common.resource.composite.flt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FltIn {
	private String filterCode;
	private String query;
	private Long id;
}
