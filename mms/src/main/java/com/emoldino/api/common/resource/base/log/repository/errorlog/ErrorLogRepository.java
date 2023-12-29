package com.emoldino.api.common.resource.base.log.repository.errorlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long>, QuerydslPredicateExecutor<ErrorLog> {

}
