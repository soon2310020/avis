package saleson.api.tabTable;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.TabTableData;

public interface TabTableDataRepository extends JpaRepository<TabTableData, Long>, QuerydslPredicateExecutor<TabTableData>, TabTableDataRepositoryCustom {

	void deleteAllByRefIdInAndAndTabTableId(List<Long> refIdList, Long tabTableId);

}
