package com.emoldino.api.analysis.resource.composite.proana.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProAnaPartsItem {
	private Long id;
	private String name;
	private String partCode;
}
