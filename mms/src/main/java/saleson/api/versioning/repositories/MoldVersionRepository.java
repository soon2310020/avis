package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.clone.MoldVersion;

public interface MoldVersionRepository extends JpaRepository<MoldVersion, Long>, QuerydslPredicateExecutor<MoldVersion> {
}
