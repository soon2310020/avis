package saleson.api.part;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.PartProjectProduced;

@Deprecated
public interface PartProjectProducedRepository extends JpaRepository<PartProjectProduced, Long>, QuerydslPredicateExecutor<PartProjectProduced>, PartProjectProducedRepositoryCustom {
    Optional<PartProjectProduced> findByPartIdAndProjectId(Long partId, Long projectId);
}
