package com.emoldino.api.common.resource.base.code.dto;

import org.springframework.util.ObjectUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeType {
	public CodeType(String codeType, String name, CodeField... fields) {
		this.codeType = codeType;
		this.name = name;
		if (!ObjectUtils.isEmpty(fields)) {
			int i = 0;
			for (CodeField field : fields) {
				i++;
				if (i == 1) {
					group1Field = field;
				} else if (i == 2) {
					group2Field = field;
				}
			}
		}
	}

	@ApiModelProperty(value = "Code Type")
	private String codeType;
	@ApiModelProperty(value = "Name of This Code Type")
	private String name;
	@ApiModelProperty(value = "Group 1 Field Metadata of This Code Type")
	private CodeField group1Field;
	@ApiModelProperty(value = "Group 2 Field Metadata of This Code Type")
	private CodeField group2Field;
}
