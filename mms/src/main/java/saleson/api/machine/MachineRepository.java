package saleson.api.machine;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.dto.SystemNoteParam;
import saleson.model.Company;
import saleson.model.Machine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long>, QuerydslPredicateExecutor<Machine>, MachineRepositoryCustom {
 
	Page<Machine> findAll(Predicate predicate, Pageable pageable);
	
	Optional<Machine> findByMachineCode(String machineCode);
	
	@Query("select new saleson.dto.SystemNoteParam(o.machineCode, o.id) from Machine o where upper(machineCode) like :code")
	List<SystemNoteParam> findAllByMachineCodeUpperCase(String code);

	Boolean existsByMachineCode(String code);
	Boolean existsByMachineCodeAndIdNot(String code,Long id);

	List<Machine> findAllByEnabledIsTrue();

	List<Machine> findByCompanyIdAndEnabledIsTrue(Long companyId);

	@Query("select distinct m.line from Machine m where m.enabled = true and m.companyId in :companyIds and m.company.isEmoldino = false order by m.line asc")
	List<String> findDistinctLines(List<Long> companyIds);


	List<Machine> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);

	List<Machine> findAllByIdIn(List<Long> idList);
}
