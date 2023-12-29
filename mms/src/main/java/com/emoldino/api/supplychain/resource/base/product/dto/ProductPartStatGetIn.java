package com.emoldino.api.supplychain.resource.base.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPartStatGetIn {
	private String filterCode;
	private Long productId;
	private Long partId;
	private Long moldId;
	private List<Long> supplierId;
	private List<String> month;
	private List<String> week;
}
