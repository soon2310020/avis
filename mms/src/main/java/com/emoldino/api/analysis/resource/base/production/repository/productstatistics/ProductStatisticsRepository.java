package com.emoldino.api.analysis.resource.base.production.repository.productstatistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

@Deprecated
public interface ProductStatisticsRepository extends JpaRepository<ProductStatistics, Long>, QuerydslPredicateExecutor<ProductStatistics> {

}
