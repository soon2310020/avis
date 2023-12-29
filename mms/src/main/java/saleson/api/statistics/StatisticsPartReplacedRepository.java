package saleson.api.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.StatisticsPartReplaced;

public interface StatisticsPartReplacedRepository extends JpaRepository<StatisticsPartReplaced, Long>, QuerydslPredicateExecutor<StatisticsPartReplaced> {
}
