package com.emoldino.api.common.resource.composite.pmsstp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PmsStpItem {
	private String id;
	private String name;
	private String type;
	private Boolean permitted;
	private Boolean editable;
}
