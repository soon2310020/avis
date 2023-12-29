package com.emoldino.api.common.resource.composite.codviw.dto;

import lombok.Data;

@Data
public class CodViwCodeTypeDataIn {
	private String query;
	private String group1Code;
	private String group2Code;
	private String enabled;
}