package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saleson.model.clone.CompanyVersion;

@Repository
public interface CompanyVersionRepository extends JpaRepository<CompanyVersion, Long> {
}
