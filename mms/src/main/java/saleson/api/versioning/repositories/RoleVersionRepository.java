package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.RoleVersion;

public interface RoleVersionRepository extends JpaRepository<RoleVersion, Long> {
}
