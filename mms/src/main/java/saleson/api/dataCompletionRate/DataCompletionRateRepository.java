package saleson.api.dataCompletionRate;

import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DataCompletionRate;

import java.util.Optional;

public interface DataCompletionRateRepository extends JpaRepository<DataCompletionRate, Long>, QuerydslPredicateExecutor<DataCompletionRate>, DataCompletionRateRepositoryCustom {

    Optional<DataCompletionRate> findByCompanyId(Long companyId);
}
