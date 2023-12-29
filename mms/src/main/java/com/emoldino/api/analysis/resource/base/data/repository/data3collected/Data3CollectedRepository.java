package com.emoldino.api.analysis.resource.base.data.repository.data3collected;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface Data3CollectedRepository extends JpaRepository<Data3Collected, Long>, QuerydslPredicateExecutor<Data3Collected> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Data3Collected> findWithPessimisticLockById(Long id);
}
