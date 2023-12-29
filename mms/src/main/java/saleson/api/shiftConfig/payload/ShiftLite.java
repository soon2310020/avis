package saleson.api.shiftConfig.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftLite {
    private String name;
    private Integer shiftNumber;
}
