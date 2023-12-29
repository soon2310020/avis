package com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AiMoldFeatureRepository extends JpaRepository<AiMoldFeature, Long>, QuerydslPredicateExecutor<AiMoldFeature> {
	AiMoldFeature findByMoldId(Long MoldId);
}
