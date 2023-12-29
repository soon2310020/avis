package saleson.api.shiftConfig.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.HourShift;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourShiftData {
    private Long id;
    private Long dayShiftId;
    private Long shiftNumber;
    private String start;
    private String end;

    public HourShiftData(HourShift hourShift) {
        this.id = hourShift.getId();
        this.dayShiftId = hourShift.getDayShiftId();
        this.shiftNumber = hourShift.getShiftNumber();
        this.start = hourShift.getStart();
        this.end = hourShift.getEnd();
    }

    public HourShiftData(Long shiftNumber, String start, String end) {
        this.shiftNumber = shiftNumber;
        this.start = start;
        this.end = end;
    }
}
