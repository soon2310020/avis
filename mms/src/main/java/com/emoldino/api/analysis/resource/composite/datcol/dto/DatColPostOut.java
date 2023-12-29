package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatColPostOut {
	public DatColPostOut(String serverTime) {
		this.serverTime = serverTime;
	}

	private String serverTime;
	private Map<String, List<DatColCommand>> commands;
}
