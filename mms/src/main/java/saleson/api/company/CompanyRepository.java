package saleson.api.company;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.CompanyType;
import saleson.model.Company;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long>, QuerydslPredicateExecutor<Company>, CompanyRepositoryCustom {

	List<Company> findByCompanyType(CompanyType type);

	Boolean existsCompaniesByCompanyCode(String companyCode);
	Boolean existsCompaniesByName(String name);

	Boolean existsCompaniesByCompanyCodeAndIdNot(String companyCode, Long id);
	Boolean existsCompaniesByNameAndIdNot(String name, Long id);

	Optional<Company> findByCompanyCode(String emoldino);

	List<Company> findByNameIn(List<String> names);

	List<Company> findByName(String name);

	List<Company> findByEnabled(boolean enabled);

	List<Company> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);

	List<Company> findByIdIn(List<Long> ids);
}
