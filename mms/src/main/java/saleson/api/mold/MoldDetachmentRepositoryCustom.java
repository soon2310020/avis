package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldDetachment;

import java.util.List;

public interface MoldDetachmentRepositoryCustom {

    List<MoldDetachment> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);
    List<MoldDetachment> findAllOrderByStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
}
