package com.emoldino.api.common.resource.base.masterdata.repository.mold2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface Mold2Repository extends JpaRepository<Mold2, Long>, QuerydslPredicateExecutor<Mold2> {

}
