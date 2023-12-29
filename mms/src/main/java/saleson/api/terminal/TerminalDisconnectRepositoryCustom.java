package saleson.api.terminal;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.model.TerminalDisconnect;

import java.util.List;

public interface TerminalDisconnectRepositoryCustom {

    List<TerminalDisconnect> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<TerminalDisconnect> findAllOrderByAlertDate(Predicate predicate, Pageable pageable);
}
