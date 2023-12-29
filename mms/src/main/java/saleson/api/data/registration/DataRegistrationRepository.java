package saleson.api.data.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DataRegistration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DataRegistrationRepository extends JpaRepository<DataRegistration, Long>, QuerydslPredicateExecutor<DataRegistration>, DataRegistrationRepositoryCustom {

    @Query(value = "SELECT max(requestIndex) FROM DataRegistration")
    Optional<BigDecimal> findMaxIndex();

    Optional<List<DataRegistration>> findByCompleted(boolean completed);
    Optional<List<DataRegistration>> findByCompletedAndSentMailDayAndLastReminded(boolean completed, String day, boolean lastReminded);
}
