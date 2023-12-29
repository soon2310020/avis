package saleson.api.rejectedPart;

import org.springframework.data.domain.Pageable;
import saleson.api.rejectedPart.payload.MachineCountEntryRecord;
import saleson.api.rejectedPart.payload.RejectRatePayload;
import saleson.dto.RejectPartEntryRecordItemDTO;
import saleson.dto.RejectRateOEEDTO;
import saleson.model.rejectedPartRate.RejectedPartDetails;

import java.util.List;

public interface ProducedPartRepositoryCustom {
    List<RejectRateOEEDTO> findRejectRateOEE(RejectRatePayload payload, Pageable pageable, String startHour, String endHour);

    long countRejectRateOEE(RejectRatePayload payload, Pageable pageable, String startHour, String endHour);

    long countEntryRecord(Long machineId, String day, String startHour, String endHour, Pageable pageable);

    List<RejectPartEntryRecordItemDTO> findRejectPartEntryRecord(Long machineId, String day, Pageable pageable, String startHour, String endHour);

    List<Long> findAllMachineIdRejectRateOEE(RejectRatePayload payload, String startHour, String endHour);

    List<MachineCountEntryRecord> countMachineRejectRateOEE(List<Long> machineIdList, String day, String startHour, String endHour, Pageable pageable);
}
