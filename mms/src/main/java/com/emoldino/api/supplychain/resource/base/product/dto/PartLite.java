package com.emoldino.api.supplychain.resource.base.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartLite {
	private Long id;
	private String name;
	private String partCode;
}
