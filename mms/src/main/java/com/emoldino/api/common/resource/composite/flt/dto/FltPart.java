package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Part;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FltPart {
	private Long id;
	private String name;
	private String partCode;

	private Long categoryId;

	public FltPart(Part part) {
		ValueUtils.map(part, this);
	}

	public FltPart(Long id, String name, String partCode) {
		this(id, name, partCode, null);
	}

	public String getCode() {
		return partCode;
	}

}
