package com.emoldino.api.supplychain.resource.base.product.repository.partdemand;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class PartDemandRepositoryImpl extends QuerydslRepositorySupport {
	public PartDemandRepositoryImpl() {
		super(PartDemand.class);
	}
}
