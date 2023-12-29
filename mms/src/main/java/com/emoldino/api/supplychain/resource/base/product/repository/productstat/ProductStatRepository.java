package com.emoldino.api.supplychain.resource.base.product.repository.productstat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductStatRepository extends JpaRepository<ProductStat, Long>, QuerydslPredicateExecutor<ProductStat>, ProductStatRepositoryCustom {
	List<ProductStat> findAllByProductIdAndWeekOrderByDay(Long productId, String week);
}
