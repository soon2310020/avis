package saleson.api.rejectedPart;

import org.springframework.data.domain.Pageable;
import saleson.common.enumeration.Frequent;
import saleson.model.data.rejectedPartRate.RejectedPartBreakDownData;
import saleson.model.rejectedPartRate.RejectedPartDetails;

import java.util.List;

public interface RejectedPartDetailsRepositoryCustom {
    List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndPeriod(Long moldId, Long partId, String startDate, String endDate);

    List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndFrequent(Long moldId, Long partId, Frequent frequent, String selectedTime);

    List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndFrequentAndPeriod(Long moldId, Long partId, Frequent frequent, String start, String end);

    List<RejectedPartDetails> getRejectedPartDetailsByMachineIdAndDay(Long machineId, String day, Pageable pageable, String startHour, String endHour);

    long countRejectedPartDetailsByMachineIdAndDay(Long machineId, String day, String startHour, String endHour);
}
