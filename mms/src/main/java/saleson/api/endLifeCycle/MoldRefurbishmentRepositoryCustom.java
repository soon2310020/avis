package saleson.api.endLifeCycle;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldRefurbishment;
import saleson.model.data.MoldMisconfigureExtraData;
import saleson.model.data.MoldRefurbishmentExtraData;

import java.util.List;

public interface MoldRefurbishmentRepositoryCustom {
    List<MoldRefurbishmentExtraData> findMoldRefurbishmentOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);
    List<MoldRefurbishment> findMoldRefurbishmentOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldRefurbishment> findMoldRefurbishmentOrderByStatus(Predicate predicate, Pageable pageable);
}
