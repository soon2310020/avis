package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldDisconnect;
import saleson.model.data.MoldDisconnectExtraData;
import saleson.model.data.MoldMaintenanceExtraData;

import java.util.List;

public interface MoldDisconnectRepositoryCustom {
    List<MoldDisconnectExtraData> findMoldDisconnectOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);
    List<MoldDisconnect> findMoldDisconnectOrderByOperatingStatus(Predicate predicate, Pageable pageable);
    List<MoldDisconnect> findMoldDisconnectOrderByStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);

}
