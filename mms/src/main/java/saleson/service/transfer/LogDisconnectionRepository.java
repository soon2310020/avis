package saleson.service.transfer;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Event;
import saleson.model.logs.LogDisconnection;

@Repository
public interface LogDisconnectionRepository extends JpaRepository<LogDisconnection, Long>, QuerydslPredicateExecutor<LogDisconnection>, LogDisconnectionRepositoryCustom {
	@Query(value = "SELECT * FROM LOG_DISCONNECTION WHERE EQUIPMENT_TYPE = :type AND EQUIPMENT_ID = :id ORDER BY CREATED_AT DESC", nativeQuery = true)
	List<LogDisconnection> findAllByEquipmentTypeAndEquipmentId(@Param("type") String type, @Param("id") Long id);

	List<LogDisconnection> findByEquipmentIdAndEquipmentType(Long id, EquipmentType type, Sort sort);

	List<LogDisconnection> findByAlertId(Long alertId, Sort sort);

	List<LogDisconnection> findByAlertIdIsIn(List<Long> alertIds, Sort sort);

	List<LogDisconnection> findByEquipmentTypeAndEvent(EquipmentType equipmentType, Event event);
}
