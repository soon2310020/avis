package saleson.api.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.PriorityType;
import saleson.model.RiskLevel;

import java.util.Optional;

public interface RiskLevelRepository  extends JpaRepository<RiskLevel, Long>, QuerydslPredicateExecutor<RiskLevel>, RiskLevelRepositoryCustom {
    Optional<RiskLevel> findByName(PriorityType name);
}
