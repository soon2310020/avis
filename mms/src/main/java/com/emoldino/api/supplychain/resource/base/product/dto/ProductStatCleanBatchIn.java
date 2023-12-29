package com.emoldino.api.supplychain.resource.base.product.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductStatCleanBatchIn {
	private List<Long> productId;
	private boolean allProductStats;
	private boolean eachProductStats;
	private List<Long> partId;
	private boolean allPartStats;
	private boolean eachPartStats;
}
