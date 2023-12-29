package com.emoldino.api.common.resource.composite.codstp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodStpDataItem {
	private Long id;
	private Long companyId;
	private String companyCode;
	private String codeType; 
	private String code;
	private String title;
	private String description; 
	private String group1Code;	
	private String group2Code;
}