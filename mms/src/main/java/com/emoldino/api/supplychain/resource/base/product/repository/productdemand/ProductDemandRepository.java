package com.emoldino.api.supplychain.resource.base.product.repository.productdemand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductDemandRepository extends JpaRepository<ProductDemand, Long>, QuerydslPredicateExecutor<ProductDemand> {

}
