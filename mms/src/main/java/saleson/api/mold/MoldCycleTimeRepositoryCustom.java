package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldCycleTime;
import saleson.model.MoldCycleTimeCustomFieldValue;
import saleson.model.data.MoldCycleTimeExtraData;
import saleson.model.data.MoldCycleTimePartExtraData;

import java.util.List;

public interface MoldCycleTimeRepositoryCustom {
    List<MoldCycleTimePartExtraData> findMoldCycleTimeExtraData(Predicate predicate, Pageable pageable);

    List<MoldCycleTimeCustomFieldValue> findMoldCycleTimeCustomFieldValue(Predicate predicate, Pageable pageable);

    List<MoldCycleTimeExtraData> findMoldCycleTimeOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

    List<MoldCycleTime> findMoldCycleTimeOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldCycleTime> findMoldCycleTimeOrderByStatus(Predicate predicate, Pageable pageable);
}
