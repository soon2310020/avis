package saleson.api.mold;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.model.Mold;
import saleson.model.MoldMaintenance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoldMaintenanceRepository extends JpaRepository<MoldMaintenance, Long>, QuerydslPredicateExecutor<MoldMaintenance>,MoldMaintenanceRepositoryCustom {
    List<MoldMaintenance> findByMaintenanceStatusIsIn(List<MaintenanceStatus> statuses);
    Optional<MoldMaintenance> findFirstByMoldAndMaintenanceStatusOrderByUpdatedAtDesc(Mold mold, MaintenanceStatus status);

    List<MoldMaintenance> findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(Instant startDate, Instant endDate, List<MaintenanceStatus> statuses, Boolean latest, Sort sort);

    List<MoldMaintenance> findByIdIsIn(List<Long> ids);

    List<MoldMaintenance> findByMoldIdOrderByIdDesc(Long moldId);

    Optional<MoldMaintenance> findByMoldIdAndMaintenanceStatusAndLatest(Long moldId, MaintenanceStatus status, Boolean latest);

    Optional<MoldMaintenance> findByMoldIdAndMaintenanceStatusIsInAndLatest(Long moldId, List<MaintenanceStatus> status, Boolean latest);

    List<MoldMaintenance> findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(Long moldId, List<MaintenanceStatus> status, Boolean latest);

    List<MoldMaintenance> findAllByMaintenanceStatusIsInAndLatestOrderByIdDesc(List<MaintenanceStatus> status, Boolean latest);

    List<MoldMaintenance> findByMoldIdIsInAndMaintenanceStatusAndLatest(List<Long> moldId, MaintenanceStatus status, Boolean latest);

    Integer countByMoldId(Long moldId);
    Integer countByMoldIdAndMaintenanceStatusIn(Long moldId,List<MaintenanceStatus> maintenanceStatuses);

    List<MoldMaintenance> findByLatest(Boolean latest);
    Optional<MoldMaintenance> findFirstByMoldIdAndMaintenanceStatusIsInOrderByShotCountDesc(Long moldId, List<MaintenanceStatus> status);

    MoldMaintenance findFirstByMoldIdAndMaintenanceStatusIsInAndLatestIsTrue(Long moldId, List<MaintenanceStatus> status);

}
