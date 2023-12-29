package com.emoldino.api.analysis.resource.base.data.repository.cycletimefluctuation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CycleTimeFluctuationRepository extends JpaRepository<CycleTimeFluctuation, Long>, QuerydslPredicateExecutor<CycleTimeFluctuation> {
}
