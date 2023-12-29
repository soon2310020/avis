package com.emoldino.api.analysis.resource.base.command.repository.devicecommand;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long>, QuerydslPredicateExecutor<DeviceCommand> {

	Optional<DeviceCommand> findFirstByDeviceIdOrderByIdDesc(String deviceId);

	Optional<DeviceCommand> findFirstByDeviceIdAndIndexNoAndStatusInOrderByIdAsc(String deviceId, Integer indexNo, List<String> status);
	
	Optional<DeviceCommand> findFirstByDeviceIdAndStatusInOrderByIdDesc(String deviceId, List<String> status);

}
