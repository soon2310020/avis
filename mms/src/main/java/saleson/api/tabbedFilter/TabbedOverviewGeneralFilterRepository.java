package saleson.api.tabbedFilter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.User;
import saleson.model.filter.TabbedOverviewGeneralFilter;

import java.util.Optional;

public interface TabbedOverviewGeneralFilterRepository extends JpaRepository<TabbedOverviewGeneralFilter, Long>, QuerydslPredicateExecutor<TabbedOverviewGeneralFilter> {
    Optional<TabbedOverviewGeneralFilter> findByUser(User user);
}
