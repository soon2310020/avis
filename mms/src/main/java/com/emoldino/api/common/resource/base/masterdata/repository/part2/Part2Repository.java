package com.emoldino.api.common.resource.base.masterdata.repository.part2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface Part2Repository extends JpaRepository<Part2, Long>, QuerydslPredicateExecutor<Part2> {

}
