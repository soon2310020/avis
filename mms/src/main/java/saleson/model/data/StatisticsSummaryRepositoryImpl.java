package saleson.model.data;

import org.springframework.data.jpa.repository.support.*;

public class StatisticsSummaryRepositoryImpl extends QuerydslRepositorySupport {
	public StatisticsSummaryRepositoryImpl() {
		super(StatisticsSummary.class);
	}
}
