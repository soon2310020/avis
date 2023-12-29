package com.emoldino.api.analysis.resource.composite.datcol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DatColSenRstInfo {
	private Long moldId;
	private String moldCode;
	private Long counterId;
	private String counterCode;
	private Integer moldShotCount;
	private Integer counterShotCount;
}
