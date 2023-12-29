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
public class ProductPartDemandByWeeksGetIn {
	private Long productId;
	private Long partId;
	private List<String> weeks;
}
