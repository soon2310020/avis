package com.emoldino.api.analysis.resource.base.data.repository.transferindex;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TransferIndexRepository extends JpaRepository<TransferIndex, Long>, QuerydslPredicateExecutor<TransferIndex> {
	Long countByCiAndTff(String ci, String tff);
}
