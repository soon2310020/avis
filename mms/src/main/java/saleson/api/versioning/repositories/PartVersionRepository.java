package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saleson.model.clone.PartVersion;

@Repository
public interface PartVersionRepository extends JpaRepository<PartVersion, Long> {
}
