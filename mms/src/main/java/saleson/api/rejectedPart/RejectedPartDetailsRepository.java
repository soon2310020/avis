package saleson.api.rejectedPart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.rejectedPartRate.RejectedPartDetails;

public interface RejectedPartDetailsRepository extends JpaRepository<RejectedPartDetails, Long>, QuerydslPredicateExecutor<RejectedPartDetails>, RejectedPartDetailsRepositoryCustom {
}
