package saleson.api.tabTable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.TabTable;

public interface TabTableRepository extends JpaRepository<TabTable, Long>, QuerydslPredicateExecutor<TabTable>, TabTableRepositoryCustom {

}
