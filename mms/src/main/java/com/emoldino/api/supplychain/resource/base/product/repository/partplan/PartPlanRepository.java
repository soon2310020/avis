package com.emoldino.api.supplychain.resource.base.product.repository.partplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PartPlanRepository extends JpaRepository<PartPlan, Long>, QuerydslPredicateExecutor<PartPlan> {

}
