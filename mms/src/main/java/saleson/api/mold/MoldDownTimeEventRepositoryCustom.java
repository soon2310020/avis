package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldDowntimeEvent;

import java.util.List;

public interface MoldDownTimeEventRepositoryCustom {

    List<MoldDowntimeEvent> findMoldDownTimeEventOrderByOperatingStatus(Predicate predicate, Pageable pageable);
    List<MoldDowntimeEvent> findMoldDownTimeEventOrderByStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
}
