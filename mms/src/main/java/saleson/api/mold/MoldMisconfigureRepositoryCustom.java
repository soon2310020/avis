package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldMisconfigure;
import saleson.model.data.MoldMisconfigureExtraData;

import java.util.List;

public interface MoldMisconfigureRepositoryCustom {
    List<MoldMisconfigureExtraData> findMoldMisconfigureOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

    Page<MoldMisconfigure> findAllSortByPreset(Predicate predicate, Pageable pageable);

    List<MoldMisconfigure> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldMisconfigure> findAllOrderByStatus(Predicate predicate, Pageable pageable);
}
