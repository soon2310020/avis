package saleson.api.machine;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.RiskLevel;

public class RiskLevelRepositoryImpl extends QuerydslRepositorySupport implements RiskLevelRepositoryCustom{
    public RiskLevelRepositoryImpl() {
        super(RiskLevel.class);
    }
}
