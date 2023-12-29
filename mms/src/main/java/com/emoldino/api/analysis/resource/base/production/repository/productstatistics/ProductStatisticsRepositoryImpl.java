package com.emoldino.api.analysis.resource.base.production.repository.productstatistics;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

@Deprecated
public class ProductStatisticsRepositoryImpl extends QuerydslRepositorySupport {
	public ProductStatisticsRepositoryImpl() {
		super(ProductStatistics.class);
	}
}
