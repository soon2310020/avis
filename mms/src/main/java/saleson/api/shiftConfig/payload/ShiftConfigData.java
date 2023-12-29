package saleson.api.shiftConfig.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftConfigData {
    private Long locationId;
    private String locationCode;
    private String locationName;
    private List<DayShiftData> dayShiftData;
}
