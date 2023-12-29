package com.emoldino.api.analysis.resource.base.data.repository.transferresult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TransferResultRepository extends JpaRepository<TransferResult, Long>, QuerydslPredicateExecutor<TransferResult> {

}
