package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.UserVersion;

import java.util.Optional;

public interface UserVersionRepository extends JpaRepository<UserVersion, Long> {
    Optional<UserVersion> findById(Long id);
}
