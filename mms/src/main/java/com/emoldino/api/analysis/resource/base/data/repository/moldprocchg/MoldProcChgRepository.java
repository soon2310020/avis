package com.emoldino.api.analysis.resource.base.data.repository.moldprocchg;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MoldProcChgRepository extends JpaRepository<MoldProcChg, Long>, QuerydslPredicateExecutor<MoldProcChg> {

	Optional<MoldProcChg> findOneByMoldIdAndProcChgTime(Long moldId, String procChgTime);

}
