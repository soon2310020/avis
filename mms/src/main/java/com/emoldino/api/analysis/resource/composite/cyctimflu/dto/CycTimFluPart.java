package com.emoldino.api.analysis.resource.composite.cyctimflu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycTimFluPart {
	private Long id;
	private String name;
	private String partCode;
	private int cavity;
}
