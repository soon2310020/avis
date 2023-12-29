package saleson.api.filter;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.DashboardGeneralFilter;

public class DashboardGeneralFilterRepositoryImpl extends QuerydslRepositorySupport implements DashboardGeneralFilterRepositoryCustom{
    public DashboardGeneralFilterRepositoryImpl() {
        super(DashboardGeneralFilter.class);
    }
}
