package saleson.model.data;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.*;

public interface StatisticsSummaryRepository extends JpaRepository<StatisticsSummary, Long>, QuerydslPredicateExecutor<StatisticsSummary> {
	public int deleteAllByDayGreaterThan(int day);
}
