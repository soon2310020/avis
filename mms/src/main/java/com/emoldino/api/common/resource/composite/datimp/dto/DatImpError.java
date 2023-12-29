package com.emoldino.api.common.resource.composite.datimp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatImpError {
	private Integer rowNo;
	private Integer colNo;
	private String message;

	public DatImpError(Integer rowNo, String message) {
		this(rowNo, null, message);
	}
}
