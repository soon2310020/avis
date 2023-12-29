package com.emoldino.api.analysis.resource.base.data.repository.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DataRepository extends JpaRepository<Data, Long>, QuerydslPredicateExecutor<Data> {
	long countByRawData(String rawData);

	Optional<Data> findByRawData(String rawData);

	List<Data> findAllByRawData(String rawData);

	List<Data> findAllByExecute(boolean execute);
}
