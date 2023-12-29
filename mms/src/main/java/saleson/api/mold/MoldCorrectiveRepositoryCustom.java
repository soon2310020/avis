package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldCorrective;
import saleson.model.MoldCorrectiveCustomFieldValue;
import saleson.model.data.MoldCorrectiveExtraData;
import saleson.model.data.MoldCorrectivePartExtraData;

import java.util.List;

public interface MoldCorrectiveRepositoryCustom
{
    List<MoldCorrectivePartExtraData> findMoldCorrectivePartExtraData(Predicate predicate, Pageable pageable);

    List<MoldCorrectiveCustomFieldValue> findMoldCorrectiveCustomFieldValue(Predicate predicate, Pageable pageable);

    List<MoldCorrectiveExtraData> findMoldCorrectiveOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

    List<MoldCorrective> findMoldCorrectiveOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldCorrective> findMoldCorrectiveOrderByStatus(Predicate predicate, Pageable pageable);
}
