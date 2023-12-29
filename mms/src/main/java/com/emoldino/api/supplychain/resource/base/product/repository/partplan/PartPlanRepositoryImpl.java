package com.emoldino.api.supplychain.resource.base.product.repository.partplan;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class PartPlanRepositoryImpl extends QuerydslRepositorySupport {
	public PartPlanRepositoryImpl() {
		super(PartPlan.class);
	}
}
