package saleson.api.terminal;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import saleson.common.enumeration.NotificationStatus;
import saleson.model.TerminalDisconnect;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalDisconnectRepository extends JpaRepository<TerminalDisconnect, Long>, QuerydslPredicateExecutor<TerminalDisconnect>, TerminalDisconnectRepositoryCustom {
    List<TerminalDisconnect> findByIdIsIn(List<Long> ids);

    List<TerminalDisconnect> findAllByNotificationStatus(NotificationStatus alert);

    List<TerminalDisconnect> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

    List<TerminalDisconnect> findAllByCreatedAtBetweenAndNotificationStatusAndLatest(Instant startDate, Instant endDate, NotificationStatus status, Boolean latest, Sort sort);

    List<TerminalDisconnect> findByTerminalIdAndNotificationStatus(Long id, NotificationStatus status);

    List<TerminalDisconnect> findByTerminalIdIsInAndNotificationStatus(List<Long> ids, NotificationStatus status);

    Optional<TerminalDisconnect> findByTerminalIdAndNotificationStatusAndLatest(Long terminalId, NotificationStatus status, Boolean latest);

    List<TerminalDisconnect> findByTerminalIdAndNotificationStatusIsInAndLatest(Long terminalId, List<NotificationStatus> status, Boolean latest);
    List<TerminalDisconnect> findByNotificationStatusIsInAndLatest(List<NotificationStatus> status, Boolean latest);

    List<TerminalDisconnect> findByLatest(Boolean latest);
}
