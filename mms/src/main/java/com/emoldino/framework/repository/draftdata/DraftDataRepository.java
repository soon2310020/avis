package com.emoldino.framework.repository.draftdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DraftDataRepository extends JpaRepository<DraftData, Long>, QuerydslPredicateExecutor<DraftData> {

}
