package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformancePreDoneData {
    private long actualProductProduced;

    private double totalCttInSeconds;
    private double avgCtt;
    private int totalCavity;
}
