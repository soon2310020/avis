package saleson.api.location;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import saleson.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long>, QuerydslPredicateExecutor<Location>, LocationRepositoryCustom {

	boolean existsByCompanyId(Long id);

	Optional<Location> findByLocationCode(String locationCode);

	Optional<Location> findByName(String name);

	List<Location> findByLocationCodeAndEnabledIsTrue(String locationCode);

	Boolean existsLocationsByLocationCode(String locationCode);

	@Query("select l.id from Location l where l.locationCode = :locationCode and lower(l.company.name) <> :companyName")
	Long existsLocationsByLocationCodeAndCompanyName(@Param("locationCode") String locationCode, @Param("companyName") String companyName);

	Boolean existsLocationsByName(String name);

	@Query("select l.id from Location l where l.name = :name and lower(l.company.name) <> :companyName")
	Long existsLocationsByNameAndCompanyName(@Param("name") String name, @Param("companyName") String companyName);

	Boolean existsLocationsByLocationCodeAndIdNot(String locationCode, Long id);

	@Query("select l.id from Location l where l.locationCode = :locationCode and l.id <> :id and lower(l.company.name) <> :companyName")
	Long existsLocationsByLocationCodeAndIdNotAndCompanyName(@Param("locationCode") String locationCode, @Param("id") Long id, @Param("companyName") String companyName);

	Boolean existsLocationsByNameAndIdNot(String name, Long id);

	@Query("select l.id from Location l where l.name = :name and l.id <> :id and lower(l.company.name) <> :companyName")
	Long existsLocationsByNameAndIdNotAndCompanyName(@Param("name") String name, @Param("id") Long id, @Param("companyName") String companyName);

	List<Location> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);
	List<Location> findByCompanyIdAndEnabledIsTrue(Long companyId);

	Optional<Location> findFirstByLocationCode(String locationCode);

	Optional<Location> findFirstByName(String name);

}
