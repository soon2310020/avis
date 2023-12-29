package com.emoldino.api.common.resource.composite.dsh.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class DshPosition {
	private String type;
	private Boolean enabled;
	private Integer linePosition;
}
