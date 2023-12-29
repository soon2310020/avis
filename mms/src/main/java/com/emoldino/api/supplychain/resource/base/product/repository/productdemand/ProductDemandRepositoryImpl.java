package com.emoldino.api.supplychain.resource.base.product.repository.productdemand;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class ProductDemandRepositoryImpl extends QuerydslRepositorySupport {
	public ProductDemandRepositoryImpl() {
		super(ProductDemand.class);
	}
}
