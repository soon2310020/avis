package com.emoldino.api.common.resource.base.file.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePostTmpOut {
	@ApiModelProperty(value = "Temporal File ID (UUID)", required = true)
	private String id;
}
