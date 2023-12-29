package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.LocationVersion;

public interface LocationVersionRepository extends JpaRepository<LocationVersion, Long> {
}
