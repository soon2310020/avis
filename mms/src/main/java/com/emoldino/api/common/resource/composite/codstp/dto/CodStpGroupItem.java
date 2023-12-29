package com.emoldino.api.common.resource.composite.codstp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CodStpGroupItem {
	Long id;
	private String code;
	private String title;
	private String codeType;
	private Long companyId;		 	
}
