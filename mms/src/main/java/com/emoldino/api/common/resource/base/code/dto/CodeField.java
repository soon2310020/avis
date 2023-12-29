package com.emoldino.api.common.resource.base.code.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeField {
	public CodeField(CodeType codeType) {
		this.dataType = "CODE_DATA";
		this.codeType = codeType.getCodeType();
		this.name = codeType.getName();
	}

	@ApiModelProperty(value = "Data Type - CODE_DATA,...")
	private String dataType = "NA";
	@ApiModelProperty(value = "The Code Type when Data Type is CODE_DATA")
	private String codeType;
	@ApiModelProperty(value = "Name")
	private String name;
}
