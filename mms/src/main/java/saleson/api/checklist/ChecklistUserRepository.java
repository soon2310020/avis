package saleson.api.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.checklist.ChecklistUser;

public interface ChecklistUserRepository  extends JpaRepository<ChecklistUser,Long>, QuerydslPredicateExecutor<ChecklistUser> {
    void deleteAllByChecklistId(Long checklistId);
}
