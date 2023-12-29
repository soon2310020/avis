package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpectedHourlyProduction {
    private String moldCode;
    private String start;
    private String end;
    private Integer expectedProducedPart;
}
