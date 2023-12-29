package saleson.api.accessHierachy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.AccessMold;

import java.util.List;

public interface AccessMoldRepository extends JpaRepository<AccessMold, Long>, QuerydslPredicateExecutor<AccessMold> {

    List<AccessMold> findAllByCompanyId(Long companyId);
    List<AccessMold> findAllByMoldId(Long moldId);
    List<AccessMold> findAllByCompanyIdAndAccessCompanyRelationId(Long companyId,Long accessCompanyRelationId);
    List<AccessMold> findAllByAccessCompanyRelationId(Long accessCompanyRelationId);
    List<AccessMold> findAllByAccessCompanyRelationIdIn(List<Long> accessCompanyRelationIdList);

}
