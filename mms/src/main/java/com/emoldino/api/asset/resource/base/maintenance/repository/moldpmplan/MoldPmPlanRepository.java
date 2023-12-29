package com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;

public interface MoldPmPlanRepository extends JpaRepository<MoldPmPlan, Long>, QuerydslPredicateExecutor<MoldPmPlan> {

	Optional<MoldPmPlan> findByMoldId(Long moldId);

	List<MoldPmPlan> findAllByPmStrategyAndNextUpcomingToleranceDate(PM_STRATEGY pmStrategy, String nextUpcomingToleranceDate);

}
