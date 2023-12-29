package saleson.api.mold.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwitchMoldPartPayload {
    private Long moldId;
    private Long currentPartId;
    private Long toBePartId;
    private boolean happensNow;
    private Long switchedTime;
}
