package com.emoldino.api.analysis.resource.composite.cdtisp.dto;

import java.util.List;

import lombok.Data;

@Data
public class CdtIspAdjustIn {
	private List<String> ci;
	private String lastDate;
}
