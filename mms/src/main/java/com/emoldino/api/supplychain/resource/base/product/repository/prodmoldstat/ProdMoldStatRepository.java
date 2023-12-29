package com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProdMoldStatRepository extends JpaRepository<ProdMoldStat, Long>, QuerydslPredicateExecutor<ProdMoldStat> {

}
