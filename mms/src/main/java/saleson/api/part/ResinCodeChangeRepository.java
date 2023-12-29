package saleson.api.part;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.ResinCodeChange;

import java.util.List;

public interface ResinCodeChangeRepository extends JpaRepository<ResinCodeChange, Long>, QuerydslPredicateExecutor<ResinCodeChange> {
    List<ResinCodeChange> findByHour(String hour);
    List<ResinCodeChange> findByDay(String day);
    List<ResinCodeChange> findByWeek(String week);
    List<ResinCodeChange> findByMonth(String month);
}
