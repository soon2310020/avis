package com.emoldino.api.integration.resource.base.ai.repository.aitsdresult;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AiTsdResultRepository extends JpaRepository<AiTsdResult, Long>, QuerydslPredicateExecutor<AiTsdResult> {

	AiTsdResult findByStatisticsId(String statisticsId);

	List<AiTsdResult> findByMoldId(Long moldId);
}
