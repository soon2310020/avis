package com.emoldino.api.supplychain.resource.base.product.repository.partstat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PartStatRepository extends JpaRepository<PartStat, Long>, QuerydslPredicateExecutor<PartStat>, PartStatRepositoryCustom {

}
