package saleson.api.counter;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.dto.SystemNoteParam;
import saleson.model.Counter;

import java.util.List;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Long>, QuerydslPredicateExecutor<Counter>, CounterRepositoryCustom {

	@EntityGraph(attributePaths = "mold")
	Page<Counter> findAll(Predicate predicate, Pageable pageable);

	boolean existsByEquipmentCodeAndEquipmentStatus(String equipmentCode, EquipmentStatus equipmentStatus);

	Optional<Counter> findByEquipmentCode(String equipmentCode);


	@Query("select new saleson.dto.SystemNoteParam(o.equipmentCode,o.id) from Counter o where upper(equipmentCode) like :code")
	List<SystemNoteParam> findAllByEquipmentCodeUpperCase(String code);

	List<Counter> findAllByEquipmentCodeIn(List<String> equimentCodes);

}
