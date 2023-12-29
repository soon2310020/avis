package com.emoldino.api.common.resource.base.option.repository.optionfieldvalue;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OptionFieldValueRepository extends JpaRepository<OptionFieldValue, Long>, QuerydslPredicateExecutor<OptionFieldValue> {

	@Query(value = "select current_timestamp() time", nativeQuery = true)
	Instant getTime();

}
