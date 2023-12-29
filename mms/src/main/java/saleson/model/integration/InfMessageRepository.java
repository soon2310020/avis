package saleson.model.integration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface InfMessageRepository extends JpaRepository<InfMessage, Long>, QuerydslPredicateExecutor<InfMessage> {

}
