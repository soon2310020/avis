package saleson.api.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.checklist.Checklist;
import saleson.model.clone.ChecklistVersion;

public interface ChecklistVersionRepository  extends JpaRepository<ChecklistVersion,Long>{
}
