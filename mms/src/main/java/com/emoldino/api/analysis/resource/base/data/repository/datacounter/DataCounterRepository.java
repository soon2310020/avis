package com.emoldino.api.analysis.resource.base.data.repository.datacounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DataCounterRepository extends JpaRepository<DataCounter, Long>, QuerydslPredicateExecutor<DataCounter> {

}
