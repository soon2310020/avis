package saleson.api.location;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.common.enumeration.ContinentName;
import saleson.model.Continent;

public interface ContinentRepository extends JpaRepository<Continent, Long>, QuerydslPredicateExecutor<Continent> {
	Optional<Continent> findByContinentNameAndCountryCode(ContinentName continentName, String countryCode);
}
