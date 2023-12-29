package saleson.api.endLifeCycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.model.Mold;
import saleson.model.MoldEndLifeCycle;
import saleson.model.MoldRefurbishment;

import java.util.List;

public interface MoldRefurbishmentRepository extends JpaRepository<MoldRefurbishment, Long>, QuerydslPredicateExecutor<MoldRefurbishment>, MoldRefurbishmentRepositoryCustom {

    //    Optional<MoldEndLifeCycleHistory> findFirstByMold(Mold mold);
    List<MoldRefurbishment> findByLatest(Boolean latest);
    List<MoldRefurbishment> findByLatestAndMoldEndLifeCycle(Boolean latest, MoldEndLifeCycle moldEndLifeCycle);
    List<MoldRefurbishment> findByMoldAndRefurbishmentStatusInAndLastChecked(Mold mold, List<RefurbishmentStatus> correctiveStatuses, Boolean lastChecked);

}
