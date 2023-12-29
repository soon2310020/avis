package saleson.api.configuration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.common.enumeration.CurrencyType;
import saleson.model.config.CurrencyConfig;

public interface CurrencyConfigRepository extends JpaRepository<CurrencyConfig, Long>, QuerydslPredicateExecutor<CurrencyConfig> {

	List<CurrencyConfig> findAllByDeletedIsFalseOrderByMainDesc();

	Optional<CurrencyConfig> findByCurrencyType(CurrencyType currencyType);

	List<CurrencyConfig> findAllByMainIsTrue();
}
