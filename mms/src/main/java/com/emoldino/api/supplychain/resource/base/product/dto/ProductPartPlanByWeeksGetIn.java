package com.emoldino.api.supplychain.resource.base.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPartPlanByWeeksGetIn {
	private Long productId;
	private Long partId;
	private List<Long> supplierId;
	private List<String> weeks;

	public String getFilterCode() {
		return "COMMON";
	}
}
