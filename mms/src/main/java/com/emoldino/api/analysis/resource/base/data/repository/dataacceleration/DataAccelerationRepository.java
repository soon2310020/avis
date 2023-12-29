package com.emoldino.api.analysis.resource.base.data.repository.dataacceleration;

import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DataAccelerationRepository extends JpaRepository<DataAcceleration, Long>, QuerydslPredicateExecutor<DataAcceleration>, DataAccelerationRepositoryCustom {

	List<DataAcceleration> findAllByCounterIdAndMeasurementDateStartsWithOrderByMeasurementDate(String counterId, String date);

	Optional<DataAcceleration> findFirstByCounterIdOrderByMeasurementDateAsc(String counterId);

	Optional<DataAcceleration> findFirstByCounterIdOrderByMeasurementDateDesc(String counterId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<DataAcceleration> findWithLockById(Long id);

}
