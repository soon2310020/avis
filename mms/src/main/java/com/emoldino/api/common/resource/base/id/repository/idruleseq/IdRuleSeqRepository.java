package com.emoldino.api.common.resource.base.id.repository.idruleseq;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;

@Repository
public interface IdRuleSeqRepository extends JpaRepository<IdRuleSeq, Long>, QuerydslPredicateExecutor<IdRuleSeq> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<IdRuleSeq> findByIdRuleCodeAndValuePattern(IdRuleCode idRuleCode, String valuePattern);

}