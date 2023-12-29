package saleson.api.configuration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.ConfigCategory;
import saleson.model.config.GeneralConfig;

import java.util.List;
import java.util.Optional;

public interface GeneralConfigRepository extends JpaRepository<GeneralConfig, Long>, QuerydslPredicateExecutor<GeneralConfig> {

	Optional<GeneralConfig> findById(Long id);

	Optional<GeneralConfig> findByConfigCategoryAndFieldName(ConfigCategory configCategory, String fieldName);

	Optional<List<GeneralConfig>> findByConfigCategoryAndDeletedFieldIsTrue(ConfigCategory category);

}
