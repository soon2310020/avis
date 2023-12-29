package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.CounterVersion;

public interface CounterVersionRepository extends JpaRepository<CounterVersion, Long> {
}
