package saleson.api.checklist;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.Company;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;

import java.util.List;

public interface ChecklistRepositoryCustom {
    Page<Checklist> getAllOrderBySpecialField(Predicate predicate, Pageable pageable);

    List<Checklist> findAllByCompanyIdAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType);


    List<Checklist> findAllByCompanyContainsAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType);
}
