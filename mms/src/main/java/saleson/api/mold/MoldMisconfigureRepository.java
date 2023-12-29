package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.model.MoldMisconfigure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldMisconfigureRepository extends JpaRepository<MoldMisconfigure, Long>, QuerydslPredicateExecutor<MoldMisconfigure>, MoldMisconfigureRepositoryCustom {

	Optional<MoldMisconfigure> findByMoldIdAndMisconfigureStatus(Long id, MisconfigureStatus status);

	List<MoldMisconfigure> findByCounterIdAndMisconfigureStatusAndLatest(Long counterId, MisconfigureStatus status, Boolean latest);

	List<MoldMisconfigure> findByCounterIdAndMisconfigureStatusInAndLatest(Long counterId, List<MisconfigureStatus> status, Boolean latest);

	List<MoldMisconfigure> findByMisconfigureStatus(MisconfigureStatus status);

	List<MoldMisconfigure> findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(Instant startDate, Instant endDate, MisconfigureStatus status, Boolean latest, Sort sort);

	List<MoldMisconfigure> findByLatest(Boolean latest);
}
