package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.MachineVersion;

public interface MachineVersionRepository extends JpaRepository<MachineVersion, Long> {
}
