package com.emoldino.api.integration.resource.base.ai.repository.aipqresult;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPqResultRepository extends JpaRepository<AiPqResult, Long>, QuerydslPredicateExecutor<AiPqResult> {
	List<AiPqResult> findByCtStatus(String ctStatus);
}
