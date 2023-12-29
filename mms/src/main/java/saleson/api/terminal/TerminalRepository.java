package saleson.api.terminal;


import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.dto.SystemNoteParam;
import saleson.model.Terminal;
import saleson.model.data.TerminalData;

import java.util.List;
import java.util.Optional;

public interface TerminalRepository extends JpaRepository<Terminal, Long>, QuerydslPredicateExecutor<Terminal>, TerminalRepositoryCustom {
	boolean existsByEquipmentCode(String equipmentCode);

	Optional<Terminal> findByEquipmentCode(String ti);

	boolean existsByLocationId(Long id);

	List<Terminal> findAllByOperatingStatus(OperatingStatus working);

	List<Terminal> findAllByOperatingStatusAndEquipmentStatus(OperatingStatus working, EquipmentStatus status);

	@Query("select new saleson.dto.SystemNoteParam(o.equipmentCode,o.id) from Terminal o where upper(equipmentCode) like :code")
	List<SystemNoteParam> findAllByEquipmentCodeUpperCase(String code);

	List<Terminal> findAllByLocationId(Long locationId);
}
