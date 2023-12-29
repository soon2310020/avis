package saleson.api.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;

import java.util.List;
import java.util.Optional;

public interface ChecklistRepository extends JpaRepository<Checklist,Long> , QuerydslPredicateExecutor<Checklist>, ChecklistRepositoryCustom {
    List<Checklist> findAllByCompanyIdAndChecklistTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType);
    List<Checklist> findAllByCompanyIdAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType);
    Optional<Checklist> findFirstByCompanyIdAndChecklistTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType);
    Optional<Checklist> findFirstByCompanyIdAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType);

    @Query("select count(c) from Checklist c where upper(c.checklistCode) = upper(:checklistCode) and upper(c.checklistType) = upper(:checklistType) and (:id is null or c.id != :id)")
    long countAllByChecklistCodeAndChecklistTypeExist(@Param("checklistCode") String checklistCode, @Param("checklistType") String checklistType, @Param("id")  Long id);


    @Query("select count(c) from Checklist c where upper(c.checklistCode) = upper(:checklistCode) and upper(c.checklistType) = upper(:checklistType) and upper(c.objectType) = upper(:checklistObjectType) and (:id is null or c.id != :id)")
    long countAllByChecklistCodeAndChecklistTypeAndObjectTypeExist(@Param("checklistCode") String checklistCode, @Param("checklistType") String checklistType, @Param("checklistObjectType") String checklistObjectType, @Param("id")  Long id);


    @Query("select count(c) from Checklist c where upper(c.checklistCode) = upper(:checklistCode) and upper(c.objectType) = upper(:checklistObjectType) and (:id is null or c.id != :id)")
    long countAllByChecklistCodeAndObjectTypeExist(@Param("checklistCode") String checklistCode, @Param("checklistObjectType") String checklistObjectType, @Param("id")  Long id);

    List<Checklist> findAllByObjectTypeAndEnabledTrue(CheckListObjectType objectType);

    List<Checklist> findAllByObjectTypeAndChecklistTypeAndEnabledTrue(CheckListObjectType objectType, ChecklistType checklistType);
}
