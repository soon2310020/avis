package saleson.api.versioning.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.clone.CategoryVersion;

public interface CategoryVersionRepository extends JpaRepository<CategoryVersion, Long> {
}
