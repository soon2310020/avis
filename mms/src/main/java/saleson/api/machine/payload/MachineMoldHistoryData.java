package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MatchStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineMoldHistoryData {
    private Instant time;
    private String moldCode;
    private String machineCode;
    private MatchStatus status;
    private String timeStr;
}
