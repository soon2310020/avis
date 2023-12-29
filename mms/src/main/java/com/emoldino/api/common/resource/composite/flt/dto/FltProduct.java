package com.emoldino.api.common.resource.composite.flt.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FltProduct {
	private Long id;
	private String name;

	public FltProduct(Category product) {
		ValueUtils.map(product, this);
	}

	public String getCode() {
		return name;
	}
}
