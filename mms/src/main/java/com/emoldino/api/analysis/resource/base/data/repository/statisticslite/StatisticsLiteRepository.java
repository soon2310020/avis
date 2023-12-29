package com.emoldino.api.analysis.resource.base.data.repository.statisticslite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface StatisticsLiteRepository extends JpaRepository<StatisticsLite, Long>, QuerydslPredicateExecutor<StatisticsLite> {

}
