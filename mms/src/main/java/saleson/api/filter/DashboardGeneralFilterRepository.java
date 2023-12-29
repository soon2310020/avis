package saleson.api.filter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DashboardGeneralFilter;
import saleson.model.User;

import java.util.Optional;

public interface DashboardGeneralFilterRepository extends JpaRepository<DashboardGeneralFilter, Long>, QuerydslPredicateExecutor<DashboardGeneralFilter> {
    Optional<DashboardGeneralFilter> findByUser(User user);
}
