package com.emoldino.api.analysis.resource.base.data.repository.statisticsext;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface StatisticsExtRepository extends JpaRepository<StatisticsExt, Long>, QuerydslPredicateExecutor<StatisticsExt> {
	Optional<StatisticsExt> findByCdataId(Long cdataId);
}
