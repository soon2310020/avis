package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineMoldPayload {
    private Long moldId;
    private Long machineId;
}
