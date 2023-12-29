package saleson.api.data.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DataRegistrationObject;

public interface DataRegistrationObjectRepository extends JpaRepository<DataRegistrationObject, Long>, QuerydslPredicateExecutor<DataRegistrationObject> {
}
