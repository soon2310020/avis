package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import saleson.api.batch.payload.IdData;

import java.util.List;

public interface MoldEfficiencyRepositoryCustom {

	List<IdData> getAllIds(Predicate predicate);
}
