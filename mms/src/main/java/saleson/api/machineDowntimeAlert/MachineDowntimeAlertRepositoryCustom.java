package saleson.api.machineDowntimeAlert;

import org.springframework.data.domain.Pageable;
import saleson.api.batch.payload.IdData;
import saleson.api.machineDowntimeAlert.payload.MachineDowntimeAlertData;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.model.Machine;
import saleson.model.MachineDowntimeAlert;

import java.time.Instant;
import java.util.List;

public interface MachineDowntimeAlertRepositoryCustom {
    List<MachineDowntimeAlertData> getMachineDowntime(SearchMachineDowntimePayload payload, Pageable pageable);

    long countMachineDowntime(SearchMachineDowntimePayload payload,Pageable pageable);

    List<MachineDowntimeAlert> findByMachineInAndDowntimeOverlapped(List<Machine> machines, Instant start, Instant end);

    List<MachineDowntimeAlert> findByMachineInAndDowntimeTypeAndDowntimeOverlapped(List<Machine> machines, Instant start, Instant end, MachineAvailabilityType downtimeType);

    List<IdData> getAllIds(SearchMachineDowntimePayload payload, Pageable pageable);
}
