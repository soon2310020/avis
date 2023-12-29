package saleson.api.rejectedPart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.RejectRateConfiguration;

public interface RejectRateConfigurationRepository extends JpaRepository<RejectRateConfiguration, Long>, QuerydslPredicateExecutor<RejectRateConfiguration> {
}
