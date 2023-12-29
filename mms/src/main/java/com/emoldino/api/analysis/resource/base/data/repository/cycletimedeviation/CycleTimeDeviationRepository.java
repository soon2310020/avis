package com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CycleTimeDeviationRepository extends JpaRepository<CycleTimeDeviation, Long>, QuerydslPredicateExecutor<CycleTimeDeviation> {

}
