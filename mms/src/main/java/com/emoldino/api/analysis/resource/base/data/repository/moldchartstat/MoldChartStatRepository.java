package com.emoldino.api.analysis.resource.base.data.repository.moldchartstat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MoldChartStatRepository extends JpaRepository<MoldChartStat, Long>, QuerydslPredicateExecutor<MoldChartStat> {

}
