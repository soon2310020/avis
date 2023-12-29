package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.model.MoldMaintenance;
import saleson.model.MoldMaintenanceCustomFieldValue;
import saleson.model.data.MoldMaintenanceExtraData;
import saleson.model.data.MoldMaintenancePartExtraData;
import saleson.model.data.dashboard.maintenance.MaintenanceContinentData;

import java.util.List;

interface MoldMaintenanceRepositoryCustom {
    List<MoldMaintenancePartExtraData> findMoldMaintenanceExtraData(Predicate predicate, Pageable pageable);

    List<MoldMaintenanceCustomFieldValue> findMoldMaintenanceCustomFieldValue(Predicate predicate, Pageable pageable);

    List<MoldMaintenanceExtraData> findMoldMaintenanceOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

    List<MaintenanceContinentData> findMoldMaintenanceGroupByContinent(TabbedOverviewGeneralFilterPayload payload, MaintenanceStatus status);
    Long countMaintenance(TabbedOverviewGeneralFilterPayload payload);

    List<MoldMaintenance> findMoldMaintenanceOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<IdData> getAllIds(Predicate predicate);
    List<MoldMaintenance> findMoldMaintenanceOrderByStatus(Predicate predicate, Pageable pageable);
}
