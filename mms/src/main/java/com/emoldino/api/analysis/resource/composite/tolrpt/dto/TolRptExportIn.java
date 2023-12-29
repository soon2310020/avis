package com.emoldino.api.analysis.resource.composite.tolrpt.dto;

import lombok.Data;

@Data
public class TolRptExportIn extends TolRptGetIn {
	/*
	* TODO :
	*  	1. Get FileInfo
	*   2. Get Context
	* */
	private String fileInfo;
	private String context;
}
