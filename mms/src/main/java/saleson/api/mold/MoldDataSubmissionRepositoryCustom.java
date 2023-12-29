package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldDataSubmission;

import java.util.List;

public interface MoldDataSubmissionRepositoryCustom {
    List<MoldDataSubmission> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldDataSubmission> findAllOrderByStatus(Predicate predicate, Pageable pageable);
}
