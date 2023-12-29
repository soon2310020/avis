package saleson.api.mold;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.model.Mold;
import saleson.model.MoldCorrective;

import java.time.Instant;
import java.util.List;

public interface MoldCorrectiveRepository extends JpaRepository<MoldCorrective, Long>, QuerydslPredicateExecutor<MoldCorrective>, MoldCorrectiveRepositoryCustom {
    List<MoldCorrective> findByMoldAndCorrectiveStatusInAndLastChecked(Mold mold, List<CorrectiveStatus> correctiveStatuses, Boolean lastChecked);

    List<MoldCorrective> findByMoldAndCorrectiveStatusInAndLatest(Mold mold, List<CorrectiveStatus> correctiveStatuses, Boolean latest);

    List<MoldCorrective> findByCorrectiveStatusNotLikeAndExpectedEndTimeBetweenAndLatest(CorrectiveStatus correctiveStatus, Instant startTime, Instant endTime, Boolean latest);

    List<MoldCorrective> findByLatest(Boolean latest);

}
