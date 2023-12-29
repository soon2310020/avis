package saleson.api.accessHierachy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.AccessCompanyRelation;

import java.util.Optional;

public interface AccessCompanyRelationRepository extends JpaRepository<AccessCompanyRelation, Long>, QuerydslPredicateExecutor<AccessCompanyRelation> {
    Optional<AccessCompanyRelation> findFirstByCompanyIdAndCompanyParentId(Long companyId,Long companyParentId);

}
