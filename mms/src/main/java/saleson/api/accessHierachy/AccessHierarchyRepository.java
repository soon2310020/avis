package saleson.api.accessHierachy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.AccessHierarchy;

import java.util.List;
import java.util.Optional;

public interface AccessHierarchyRepository extends JpaRepository<AccessHierarchy, Long>, QuerydslPredicateExecutor<AccessHierarchy>, AccessHierarchyRepositoryCustom {
    List<AccessHierarchy> findAllByCompanyIdIn(List<Long> companyIdList);
    Optional<AccessHierarchy> findFirstByCompanyId(Long companyId);
    List<AccessHierarchy> findAllByLevelLessThanEqual(Long level);
    List<AccessHierarchy> findAllByLevelGreaterThanEqual(Long level);

}
