package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.TerminalVersion;

public interface TerminalVersionRepository extends JpaRepository<TerminalVersion, Long> {
}
