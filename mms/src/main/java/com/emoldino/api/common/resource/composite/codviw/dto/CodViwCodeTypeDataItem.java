package com.emoldino.api.common.resource.composite.codviw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodViwCodeTypeDataItem {
	private Long id;
	private String code;
	private String title;
	private String description;
	private Long companyId;
	private String companyCode;
	private String group1Code;
	private String group1Title;
	private String group2Code;
	private String group2Title;
}