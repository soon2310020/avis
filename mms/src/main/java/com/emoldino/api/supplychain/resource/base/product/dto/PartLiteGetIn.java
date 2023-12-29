package com.emoldino.api.supplychain.resource.base.product.dto;

import lombok.Data;

@Data
public class PartLiteGetIn {
	private Long productId;
	private String query;
}
