package com.emoldino.api.common.resource.base.location.repository.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface AreaRepository extends JpaRepository<Area, Long>, QuerydslPredicateExecutor<Area> {

}
