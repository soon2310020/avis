package com.emoldino.api.supplychain.resource.base.product.repository.partdemand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PartDemandRepository extends JpaRepository<PartDemand, Long>, QuerydslPredicateExecutor<PartDemand> {

}
