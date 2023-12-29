package saleson.api.endLifeCycle;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.common.enumeration.PriorityType;
import saleson.model.MoldEndLifeCycle;
import saleson.model.MoldRefurbishment;

import java.util.List;

public interface MoldEndLifeCycleRepositoryCustom {
    List<MoldEndLifeCycle> findAllOrderByPriority(Predicate predicate, Pageable pageable);
    List<MoldEndLifeCycle> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    Long countByPriorityTypeAndPredicate(PriorityType priorityType, Predicate predicate);

    List<MoldEndLifeCycle> findAllOrderByAccumulatedShot(Predicate predicate, Pageable pageable);

    List<MoldEndLifeCycle> findAllOrderByPart(Predicate predicate, Pageable pageable);

    List<MoldEndLifeCycle> findAllOrderByStatus(Predicate predicate, Pageable pageable);

}
