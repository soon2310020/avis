package saleson.api.mold;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import org.springframework.data.repository.query.Param;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.model.MoldLocation;

public interface MoldLocationRepository extends JpaRepository<MoldLocation, Long>, QuerydslPredicateExecutor<MoldLocation>, MoldLocationRepositoryCustom {

	boolean existsByMoldIdAndMoldLocationStatus(Long id, MoldLocationStatus changed);

	List<MoldLocation> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

	List<MoldLocation> findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(Instant startDate, Instant endDate, MoldLocationStatus status, Boolean latest, Sort sort);

	List<MoldLocation> findByIdIsIn(List<Long> ids);

	List<MoldLocation> findByMoldIdAndMoldLocationStatusAndLatest(Long moldId, MoldLocationStatus status, Boolean latest);

	List<MoldLocation> findByMoldIdIsInAndMoldLocationStatusAndLatest(List<Long> moldIds, MoldLocationStatus status, Boolean latest);

	List<MoldLocation> findByLatest(Boolean latest);

	List<MoldLocation> findTop2ByMoldIdOrderByIdDesc(Long moldId);

	void deleteAllByMoldIdIn(List<Long> moldId);

	List<MoldLocation> findByMoldLocationStatusAndLatest(MoldLocationStatus status, Boolean latest);

	boolean existsByIdBeforeAndMoldId(Long id, Long moldId);

	List<MoldLocation> findAllByMoldIdOrderByIdDesc(Long moldId);

	@Query(value = "SELECT ml FROM MoldLocation ml WHERE ml.moldId = :moldId AND ml.locationId <> ml.previousLocationId " +
		"and ml.moldLocationStatus = 'APPROVED'  ORDER BY ml.createdAt DESC")
	List<MoldLocation> findApprovedChangeLocationByMoldOrderByCreatedAtDesc(@Param("moldId") Long moldId, Pageable pageable);


}
