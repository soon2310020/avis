package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldCustomFieldValue;
import saleson.model.MoldLocation;
import saleson.model.MoldLocationCustomFieldValue;
import saleson.model.data.MoldLocationExtraData;

import java.util.List;

public interface MoldLocationRepositoryCustom
{
    List<MoldLocationExtraData> findMoldLocationExtraData(Predicate predicate, Pageable pageable);

    List<MoldLocationCustomFieldValue> findMoldLocationCustomFieldValue(Predicate predicate, Pageable pageable);

    List<MoldLocationExtraData> findMoldLocationOrderByAccumulatedShot(Predicate predicate, Pageable pageable,  List<Long> moldIds);

    List<MoldLocation> findMoldLocationOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<MoldLocation> findMoldLocationOrderByStatus(Predicate predicate, Pageable pageable);

    Long countMoldLocationOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

    List<Long> getListMoldIdHaveMoreThan2Location();

    List<IdData> getAllIds(Predicate predicate);
}
