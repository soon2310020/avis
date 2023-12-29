package saleson.api.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.checklist.ChecklistCompany;

public interface ChecklistCompanyRepository  extends JpaRepository<ChecklistCompany,Long>, QuerydslPredicateExecutor<ChecklistCompany> {
    void deleteAllByChecklistId(Long checklistId);
}
