package com.emoldino.api.analysis.resource.composite.datcol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatColCommand {
	private String command;
	private int indexNo;
	private String data;
}
